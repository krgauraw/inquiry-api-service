package filters

import akka.util.ByteString
import org.sunbird.telemetry.logger.TelemetryManager

import javax.inject.Inject
import org.sunbird.telemetry.util.TelemetryAccessEventUtil
import play.api.Logging
import play.api.libs.streams.Accumulator
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._

class AccessLogFilter @Inject() (implicit ec: ExecutionContext) extends EssentialFilter with Logging {

    val xHeaderNames = Map("x-session-id" -> "X-Session-ID", "X-Consumer-ID" -> "x-consumer-id", "x-device-id" -> "X-Device-ID", "x-app-id" -> "APP_ID", "x-authenticated-userid" -> "X-Authenticated-Userid", "x-channel-id" -> "X-Channel-Id", "x-request-id" -> "X-Request-Id")

    def apply(nextFilter: EssentialAction) = new EssentialAction {
      def apply(requestHeader: RequestHeader) = {

        val startTime = System.currentTimeMillis
        val reqPath = requestHeader.uri
        if(!reqPath.contains("/health")){
          val reqId = requestHeader.headers.get("X-Request-Id").getOrElse(java.util.UUID.randomUUID()).asInstanceOf[String]
          val entryLogStr = s"""{"eid":"LOG","edata":{"type":"system","level":"TRACE","requestid":${reqId},"message":"ENTRY LOG: ${reqPath} : Request Received.","params":[{"key":"value"}]}}"""
          TelemetryManager.info(entryLogStr)
        }

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
            val reqId = requestHeader.headers.get("X-Request-Id").getOrElse(java.util.UUID.randomUUID()).asInstanceOf[String]
            val exitLog = s"""{"eid":"LOG","edata":{"type":"system","level":"TRACE","requestid":${reqId},"message":"EXIT LOG: ${path} | Response Provided. Response Code: ${result.header.status}","params":[{"key":"value"}]}}"""
            TelemetryManager.info(exitLog)
            TelemetryAccessEventUtil.writeTelemetryEventLog((otherDetails ++ appHeaders).asInstanceOf[Map[String, AnyRef]].asJava)

          }
          result.withHeaders("Request-Time" -> requestTime.toString)
        }
      }
    }
  }