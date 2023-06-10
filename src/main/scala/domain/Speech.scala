package domain

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import java.time.format.DateTimeFormatter
import java.time.{LocalDate}
import scala.util.control.NonFatal

case class Speech(speaker: String, topic:String, dateOfSpeech: LocalDate, wordCount:Option[Long])

object Speech {

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  implicit val dateDecoder:Decoder[LocalDate] = Decoder.decodeString.emap[LocalDate](str => {
    try {
      Right(LocalDate.parse(str, formatter))
    } catch {
      case NonFatal(e) => Left(e.getMessage)
    }
  })



  implicit val decoderSpeech: Decoder[Speech] = deriveDecoder[Speech]

  def convertToJson(inputString: String): String = {
    val values = inputString.split(",")
    s"""{"speaker": "${values(0).trim}", "topic": "${values(1).trim}",
       |"dateOfSpeech": "${values(2).trim}", "wordCount": "${values(3).trim}"}""".stripMargin
  }
}


