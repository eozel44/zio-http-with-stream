import domain.ServiceError
import zio._
import zio.http.HttpError.InternalServerError
import zio.http._

object Main extends ZIOAppDefault {

  import service._

  val app: HttpApp[Client & Scope, Nothing] = Http.collectZIO[Request] {
    case request @ Method.GET -> Root / "evaluation" =>
      SpeechService
        .calculateSpeechs(request.url.queryParams.get("url").toList.flatten)
        .foldZIO(
          { case error: ServiceError =>
            Console.printLine(error).orDie *>
              ZIO.succeed(Response.fromHttpError(InternalServerError("Internal error : " + error.getMessage)))
          },
          result =>
            ZIO.succeed(
              Response
                .json(s"""{"mostSecurity": "${result._1.getOrElse("null")}","mostSpeeches":
                         |"${result._2.getOrElse("null")}","leastWordy": "${result._3.getOrElse("null")}"}""".stripMargin)
            )
        )
  }

  override val run =
    Server.serve(app).provide(Client.default, Scope.default, Server.defaultWithPort(8090))

}
