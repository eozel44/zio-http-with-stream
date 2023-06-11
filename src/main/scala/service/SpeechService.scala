package service

import domain.{ HttpClientError, ParsingError, Speech }
import zio._
import zio.http.Client
import zio.stream._
import io.circe.parser._

import java.time.temporal.ChronoField

object SpeechService {

  import domain.Speech._

  private def sum(stream: ZStream[Any, Throwable, Option[Long]]): ZIO[Any, Throwable, Option[Long]] =
    stream.runFold(Option.empty[Long]) {
      case (None, some)       => some
      case (Some(x), Some(y)) => Some(x + y)
      case (some, None)       => some
    }

  private def calculate(
    stream: ZStream[Any, Throwable, Speech]
  ): ZStream[Any, Throwable, (String, Option[Long])] =
    stream.groupBy(in => ZIO.succeed(in.speaker, in.wordCount)) { case (speaker, stream) =>
      ZStream.fromZIO(sum(stream)).map(r => (speaker, r))
    }

  def calculateSpeechs(urls: List[String]) =
    for {

      zstreams <- ZStream
                    .fromIterable(urls)
                    .flatMap(url =>
                      ZStream
                        .fromZIO(Client.request(url).mapError(k => HttpClientError(k.getMessage)))
                        .flatMap(l => l.body.asStream)
                        .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
                        .via(ZPipeline.drop(1))
                    )
                    .via(
                      ZPipeline.mapZIO(in =>
                        ZIO.fromEither(decode[Speech](convertToJson(in))).mapError(k => ParsingError(k.getMessage()))
                      )
                    )
                    .broadcast(3, 30)

      mostSpeechs   <- calculate(
                         zstreams(0).filter(in => in.dateOfSpeech.get(ChronoField.YEAR).equals(2012))
                       ).runCollect.fork
      mostSpecurity <- calculate(zstreams(1).filter(in => in.topic.equals("Kohlesubventionen"))).runCollect.fork
      leastWordy    <- calculate(zstreams(2)).runCollect.fork

      zipped <- mostSpeechs.zip(mostSpecurity).zip(leastWordy).join

    } yield (
      zipped._1.sortWith(_._2.getOrElse(0L) > _._2.getOrElse(0L)).head._1,
      zipped._2.sortWith(_._2.getOrElse(0L) > _._2.getOrElse(0L)).head._1,
      zipped._3.sortWith(_._2.getOrElse(0L) < _._2.getOrElse(0L)).head._1
    )

}
