package service

import domain.{ HttpClientError, ParsingError, Speech }
import zio._
import zio.http.Client
import zio.stream._
import io.circe.parser._

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

  def calculateSpeechs(url: String) =
    for {

      res    <- Client.request(url).mapError(k => HttpClientError(k.getMessage))
      zstream = res.body.asStream
                  .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
                  .via(ZPipeline.drop(1))
                  .via(
                    ZPipeline.mapZIO(in =>
                      ZIO.fromEither(decode[Speech](convertToJson(in))).mapError(k => ParsingError(k.getMessage()))
                    )
                  )

      //mostSpeechs   <- calculate(zstream.filter(in=> in.dateOfSpeech.get(ChronoField.YEAR).equals(2012))).runCollect
      //mostSpeechsSpeaker = mostSpeechs.sortWith(_._2.getOrElse(0L) > _._2.getOrElse(0L)).head._1
      //_ <- Console.printLine(mostSpeechsSpeaker)

      mostSpecurity       <- calculate(zstream.filter(in => in.topic.equals("Kohlesubventionen"))).runCollect
      mostSecuritysSpeaker = mostSpecurity.sortWith(_._2.getOrElse(0L) > _._2.getOrElse(0L)).head._1
      // _                   <- Console.printLine(mostSecuritysSpeaker)
      //result                   <- Response.text(mostSecuritysSpeaker)

      //      leastWordy    <- calculate(zstream).runCollect
      //      leastWordySpeaker = leastWordy.sortWith(_._2.getOrElse(0L) < _._2.getOrElse(0L)).head._1
      //      _ <- Console.printLine(leastWordySpeaker)

    } yield mostSecuritysSpeaker

}
