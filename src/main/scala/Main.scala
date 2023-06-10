import domain.{HttpClientError, ParsingError, Speech}
import zio._
import zio.http.Client
import zio.stream._
import io.circe.parser._

import java.time.temporal.ChronoField

object Main extends ZIOAppDefault {

import service._

  val url = "https://fid-recruiting.s3-eu-west-1.amazonaws.com/politics.csv"


    val program = for {

      res  <- SpeechService.calculateSpeechs(url)

    } yield res

  override val run = program.provide(Client.default)

}