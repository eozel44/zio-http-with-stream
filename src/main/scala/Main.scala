import domain.{HttpClientError, ParsingError, Speech}
import zio._
import zio.http.Client
import zio.stream._
import io.circe.parser._

import java.time.temporal.ChronoField

object Main extends ZIOAppDefault {

import domain.Speech._

  val url = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"


  def maxSome(stream: ZStream[Any, Throwable, Option[Long]]): ZIO[Any, Throwable, Option[Long]] =
    stream.runFold(Option.empty[Long]) {
      case (None, some)       => some
      case (Some(x), Some(y)) => Some(List(x, y).max)
      case (some, None)       => some
    }

    val program = for {

      res  <- Client.request(url).mapError(k => HttpClientError(k.getMessage))
      zstream = res.body.asStream
        .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
        .via(ZPipeline.drop(1))
        .via(ZPipeline.mapZIO(in => ZIO.fromEither(decode[Speech](convertToJson(in))).mapError(k => ParsingError(k.getMessage()))))

        mostSpeechs   <- zstream
                               .filter(in=> in.dateOfSpeech.get(ChronoField.YEAR).equals(2012))
                .groupBy(in => ZIO.succeed(in.speaker, in.wordCount)) { case (speaker, stream) =>

                  ZStream.fromZIO(maxSome(stream)).map(r => (speaker, r))

                }.tap(k=>Console.printLine(k)).runCollect


//       st <- zstream.tap(k=> Console.printLine(k))
//        .runCollect


    } yield res

  override val run = program.provide(Client.default)

}