package filters

import akka.util.ByteString
import org.sunbird.telemetry.util.TelemetryAccessEventUtil
import play.api.Logging
import play.api.libs.streams.Accumulator
import play.api.mvc._

import java.util.UUID
import javax.inject.Inject
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext

class AccessLogFilter @Inject() (implicit ec: ExecutionContext) extends EssentialFilter with Logging {

    val xHeaderNames = Map("x-session-id" -> "X-Session-ID", "X-Consumer-ID" -> "x-consumer-id", "x-device-id" -> "X-Device-ID", "x-app-id" -> "APP_ID", "x-authenticated-userid" -> "X-Authenticated-Userid", "x-channel-id" -> "X-Channel-Id")

    def apply(nextFilter: EssentialAction) = new EssentialAction {
      def apply(requestHeader: RequestHeader) = {

        val startTime = System.currentTimeMillis
        val defaultReqId = UUID.randomUUID().toString

        /*requestHeader.headers.add(("X-Request-Id" -> defaultReqId))
        val newReqHeader = requestHeader*/
        /*val newReqHeader = if (requestHeader.uri.contains("/publish")) {
          val requestId = requestHeader.headers.get("X-Request-Id").getOrElse("")
          val (reqHeader, reqId) = if(StringUtils.isEmpty(requestId)) {
            val temp = requestHeader.headers.add(("X-Request-Id" -> defaultReqId))
            (temp,defaultReqId)
          } else (requestHeader, requestId)
          TelemetryManager.info(s"ENTRY:assessment: Request URL: ${reqHeader.uri} : Request Received For Publish.", Map("requestId" -> reqId).asJava.asInstanceOf[java.util.Map[String, AnyRef]])
          reqHeader
        } else requestHeader*/

        val accumulator: Accumulator[ByteString, Result] = nextFilter(requestHeader)

        accumulator.map { result =>
          val endTime     = System.currentTimeMillis
          val requestTime = endTime - startTime

          val path = requestHeader.uri
          if(!path.contains("/health")){
            val headers = requestHeader.headers.headers.groupBy(_._1).mapValues(_.map(_._2))
            val appHeaders = headers.filter(header => xHeaderNames.keySet.contains(header._1.toLowerCase))
                .map(entry => (xHeaderNames.get(entry._1.toLowerCase()).get, entry._2.head))
            val otherDetails = Map[String, Any]("StartTime" -> startTime, "env" -> "assessment",
                "RemoteAddress" -> requestHeader.remoteAddress,
                "ContentLength" -> result.body.contentLength.getOrElse(0),
                "Status" -> result.header.status, "Protocol" -> "http",
                "path" -> path,
                "Method" -> requestHeader.method.toString)
            TelemetryAccessEventUtil.writeTelemetryEventLog((otherDetails ++ appHeaders).asInstanceOf[Map[String, AnyRef]].asJava)
          }
          /*if (newReqHeader.uri.contains("/publish")) {
            val requestId = newReqHeader.headers.get("X-Request-Id").getOrElse("")
            val params = Map("requestId" -> requestId, "Status" -> result.header.status).asJava.asInstanceOf[java.util.Map[String, AnyRef]]
            TelemetryManager.info(s"EXIT:assessment: Request URL: ${newReqHeader.uri} : Response Provided.", params)
          }*/
          result.withHeaders("Request-Time" -> requestTime.toString)
        }
      }
    }

  }