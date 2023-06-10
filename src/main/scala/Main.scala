import domain.{HttpClientError, ParsingError, Speech}
import zio._
import zio.http.Client
import zio.stream._
import io.circe.parser._

object Main extends ZIOAppDefault {

import domain.Speech._

  val url = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"


  def isDateTopic(spc: Speech) = {
    true
    //spc.dateOfSpeech.getYear().equals(112) || spc.topic.equals("Innere Sicherheit")
  }

  def isTopicFilter(spc: Speech) = {
    spc.topic.equals("Innere Sicherheit")
  }

    val program = for {

      res  <- Client.request(url).mapError(k => HttpClientError(k.getMessage))
      _ <- res.body.asStream
        .via(ZPipeline.utf8Decode >>> ZPipeline.splitLines)
        .via(ZPipeline.drop(1))
        .via(ZPipeline.mapZIO(in => ZIO.fromEither(decode[Speech](convertToJson(in))).mapError(k => ParsingError(k.getMessage()))))
        .tap(k=> Console.printLine(k))
        .runCollect
//        .partition(isDateTopic,4)
//        .map{ case (leftStream, rightStream) => {
//
//          val mostSpeeches = leftStream.filterNot(isTopicFilter)
//                              .groupByKey(l=>l.speaker){
//            case (speaker, stream) => ZStream.fromZIO(stream.runCollect.map(l => speaker -> l.map(_.wordCount)))
//          }
//
//        }
//        }

    } yield res

  override val run = program.provide(Client.default)

}