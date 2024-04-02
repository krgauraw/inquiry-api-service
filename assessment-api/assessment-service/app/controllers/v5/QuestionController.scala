package controllers.v5

import akka.actor.{ActorRef, ActorSystem}
import org.sunbird.common.Platform
import play.api.mvc.ControllerComponents
import utils.{ActorNames, ApiId, QuestionOperations}

import javax.inject.{Inject, Named}
import scala.concurrent.ExecutionContext
import scala.collection.JavaConverters._

class QuestionController @Inject()(@Named(ActorNames.QUESTION_V5_ACTOR) questionActor: ActorRef, cc: ControllerComponents, actorSystem: ActorSystem)(implicit exec: ExecutionContext) extends BaseController(cc) {

  val objectType = "Question"
  val schemaName: String = "question"
  val defaultVersion:String = Platform.config.getNumber("v5_default_qumlVersion").toString

  def create() = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val question = body.getOrDefault("question", new java.util.HashMap()).asInstanceOf[java.util.Map[String, AnyRef]]
    question.putAll(headers)
    val questionRequest = getRequest(question, headers, QuestionOperations.createQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    getResult(ApiId.CREATE_QUESTION, questionActor, questionRequest)
  }

  def read(identifier: String, mode: Option[String], fields: Option[String]) = Action.async { implicit request =>
    val headers = commonHeaders()
    val question = new java.util.HashMap().asInstanceOf[java.util.Map[String, Object]]
    question.putAll(headers)
    question.putAll(Map("identifier" -> identifier, "fields" -> fields.getOrElse(""), "mode" -> mode.getOrElse("read")).asJava)
    val questionRequest = getRequest(question, headers, QuestionOperations.readQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    getResult(ApiId.READ_QUESTION, questionActor, questionRequest)
  }

  def privateRead(identifier: String, mode: Option[String], fields: Option[String]) = Action.async { implicit request =>
    val headers = commonHeaders()
    val question = new java.util.HashMap().asInstanceOf[java.util.Map[String, Object]]
    question.putAll(headers)
    question.putAll(Map("identifier" -> identifier, "fields" -> fields.getOrElse(""), "mode" -> mode.getOrElse("read")).asJava)
    val questionRequest = getRequest(question, headers, QuestionOperations.readPrivateQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    getResult(ApiId.READ_PRIVATE_QUESTION, questionActor, questionRequest)
  }

  def update(identifier: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val question = body.getOrDefault("question", new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]];
    question.putAll(headers)
    val questionRequest = getRequest(question, headers, QuestionOperations.updateQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    questionRequest.getContext.put("identifier", identifier)
    getResult(ApiId.UPDATE_QUESTION, questionActor, questionRequest)
  }

  def review(identifier: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val question = body.getOrDefault("question", new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]];
    question.putAll(headers)
    val questionRequest = getRequest(question, headers, QuestionOperations.reviewQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    questionRequest.getContext.put("identifier", identifier)
    getResult(ApiId.REVIEW_QUESTION, questionActor, questionRequest)
  }

  def publish(identifier: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    println("headers ::: "+headers)
    val body = requestBody()
    val question = body.getOrDefault("question", new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]];
    question.putAll(headers)
    val questionRequest = getRequest(question, headers, QuestionOperations.publishQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    questionRequest.getContext.put("identifier", identifier)
    getResult(ApiId.PUBLISH_QUESTION, questionActor, questionRequest)
  }

  def retire(identifier: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val question = new java.util.HashMap().asInstanceOf[java.util.Map[String, Object]]
    question.putAll(headers)
    val questionRequest = getRequest(question, headers, QuestionOperations.retireQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    questionRequest.getContext.put("identifier", identifier)
    getResult(ApiId.RETIRE_QUESTION, questionActor, questionRequest)
  }

  def importQuestion() = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    body.putAll(headers)
    val questionRequest = getRequest(body, headers, QuestionOperations.importQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    getResult(ApiId.IMPORT_QUESTION, questionActor, questionRequest)
  }

  def systemUpdate(identifier: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val content = body.getOrDefault(schemaName, new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]];
    content.putAll(headers)
    val questionRequest = getRequest(content, headers, QuestionOperations.systemUpdateQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    questionRequest.getContext.put("identifier", identifier);
    getResult(ApiId.SYSTEM_UPDATE_QUESTION, questionActor, questionRequest)
  }

  def list(fields: Option[String]) = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val question = body.getOrDefault("search", new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]];
    question.putAll(headers)
    question.put("fields", fields.getOrElse(""))
    val questionRequest = getRequest(question, headers, QuestionOperations.listQuestions.toString)
    questionRequest.put("identifiers", questionRequest.get("identifier"))
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    getResult(ApiId.LIST_QUESTIONS, questionActor, questionRequest)
  }

  def reject(identifier: String) = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val question = body.getOrDefault(schemaName, new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]];
    question.putAll(headers)
    val questionRequest = getRequest(question, headers, QuestionOperations.rejectQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    questionRequest.getContext.put("identifier", identifier)
    getResult(ApiId.REJECT_QUESTION, questionActor, questionRequest)
  }

  def copy(identifier: String, mode: Option[String]) = Action.async { implicit request =>
    val headers = commonHeaders()
    val body = requestBody()
    val question = body.getOrDefault("question", new java.util.HashMap()).asInstanceOf[java.util.Map[String, Object]];
    question.putAll(headers)
    question.putAll(Map("identifier" -> identifier, "mode" -> mode.getOrElse(""), "copyType" -> "COPY_TYPE_DEEP").asJava)
    val questionRequest = getRequest(question, headers, QuestionOperations.copyQuestion.toString)
    setRequestContext(questionRequest, defaultVersion, objectType, schemaName)
    getResult(ApiId.COPY_QUESTION, questionActor, questionRequest)
  }
}
