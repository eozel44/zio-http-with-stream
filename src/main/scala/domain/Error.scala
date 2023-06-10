package domain

trait ServiceError                    extends Exception with Product with Serializable
case class HttpError(statusCode: Int) extends ServiceError
case class ParsingError(m: String)    extends ServiceError
case class HttpClientError(m: String) extends ServiceError
