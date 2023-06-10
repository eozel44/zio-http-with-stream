import domain.{HttpClientError, ParsingError, Speech}
import zio._
import zio.http.Client
import zio.stream._
import io.circe.parser._

import java.time.temporal.ChronoField

object Main extends ZIOAppDefault {

import domain.Speech._

  val url = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"


  def sum(stream: ZStream[Any, Throwable, Option[Long]]): ZIO[Any, Throwable, Option[Long]] =
    stream.runFold(Option.empty[Long]) {
      case (None, some)       => some
      case (Some(x), Some(y)) => Some(x+y)
      case (some, None)       => some
    }

  def calculate(
                          stream: ZStream[Any, Throwable, Speech]
                        ): ZStream[Any, Throwable, (String, Option[Long])]                                          =
    stream.groupBy(in => ZIO.succeed(in.speaker, in.wordCount)) { case (speaker, stream) =>
      ZStream.fromZIO(sum(stream)).map(r => (speaker, r))
    }


    val program = for {

      res  <- Client.request(url)
      zstream = res.body.asStream
        .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
        .via(ZPipeline.drop(1))
        .via(ZPipeline.mapZIO(in => ZIO.fromEither(decode[Speech](convertToJson(in))).mapError(k => ParsingError(k.getMessage()))))

        //mostSpeechs   <- calculate(zstream.filter(in=> in.dateOfSpeech.get(ChronoField.YEAR).equals(2012))).runCollect
        //mostSpeechsSpeaker = mostSpeechs.sortWith(_._2.getOrElse(0L) > _._2.getOrElse(0L)).head._1
        //_ <- Console.printLine(mostSpeechsSpeaker)

        mostSpecurity <- calculate(zstream.filter(in=> in.topic.equals("Kohlesubventionen"))).runCollect
        mostSecuritysSpeaker = mostSpecurity.sortWith(_._2.getOrElse(0L) > _._2.getOrElse(0L)).head._1
        _ <- Console.printLine(mostSecuritysSpeaker)

//      leastWordy    <- calculate(zstream).runCollect
//      leastWordySpeaker = leastWordy.sortWith(_._2.getOrElse(0L) < _._2.getOrElse(0L)).head._1
//      _ <- Console.printLine(leastWordySpeaker)

    } yield res

  override val run = program.provide(Client.default)

}