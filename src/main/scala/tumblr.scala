package com.github.hexx.dispatch.tumblr

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import dispatch._
import dispatch.json.JsHttp._
import dispatch.json.{ Js, JsObject }
import dispatch.oauth.OAuth
import dispatch.oauth.OAuth._
import dispatch.oauth.{ Consumer, Token }

object Tumblr {
  val blog = :/("api.tumblr.com") / "v2" / "blog"
  val user = :/("api.tumblr.com") / "v2" / "user"
  val oauth = :/("www.tumblr.com") / "oauth"
}

object Auth {
  def request_token(consumer: Consumer): Handler[Token] = request_token(consumer, OAuth.oob)
  def request_token(consumer: Consumer, callback_url: String) = Tumblr.oauth.secure.POST / "request_token" <@ (consumer, callback_url) as_token

  def authorize_url(token: Token) = Tumblr.oauth / "authorize" with_token token

  def access_token(consumer: Consumer, token: Token, verifier: String) = Tumblr.oauth.secure.POST / "access_token" <@ (consumer, token, verifier) as_token
  def access_token(consumer: Consumer, username: String, password: String) = {
    val x_auth_params = Map("x_auth_mode" -> "client_auth", "x_auth_username" -> username, "x_auth_password" -> password)
    Tumblr.oauth.secure / "access_token" << x_auth_params <@ (consumer, OAuth.oob) as_token
  }
}

object Blog extends Js {
  def avatar(hostname: String) = Tumblr.blog / hostname / "avatar"
  def avatar(hostname: String, size: Int): Request = avatar(hostname) / size.toString

  def info(hostname: String, consumer: Consumer) = Tumblr.blog / hostname / "info" <<? Map("api_key" -> consumer.key) ># ('response ! ('blog ? obj))

  def post(hostname: String, consumer: Consumer, token: Token, params: (String, String)*) = new PostBuilder(hostname, consumer, token, Map(params:_*))
  def postText(hostname: String, consumer: Consumer, token: Token, body: String, params: (String, String)*) =
    post(hostname, consumer, token, params:_*).typeText body body
  def postLink(hostname: String, consumer: Consumer, token: Token, url: String, params: (String, String)*) =
    post(hostname, consumer, token, params:_*).typeLink url url

  class PostBuilder(hostname: String, consumer: Consumer, token: Token, params: Map[String, String]) extends Builder[Handler[Unit]] {
    private def param(key: String)(value: Any) = new PostBuilder(hostname, consumer, token, params + (key -> value.toString))
    def product = Tumblr.blog / hostname / "post" << params <@ (consumer, token) >|
    // All Post Types
    val posttype = param("type")_
    val state = param("state")_
    val tags = param("tags")_
    val tweet = param("teet")_
    def date(value: Date) = {
      val df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z", Locale.US)
      df.setTimeZone(TimeZone.getTimeZone("GMT"))
      param("date")(df.format(value))
    }
    def markdown(value: Boolean) = param("markdown")_
    val slug = param("slug")_
    // Text Posts
    def typeText = posttype("text")
    val title = param("title")_
    val body = param("body")_
    // Link Posts
    def typeLink = posttype("link")
    val url = param("url")_
    val description = param("description")_
  }

  val title = 'title ? str
}

object User {
  def dashboard(consumer: Consumer, token: Token, params: (String, String)*) = new DashboardBuilder(consumer, token, Map(params:_*))

  class DashboardBuilder(consumer: Consumer, token: Token, params: Map[String, String]) extends Builder[Handler[List[JsObject]]] {
    private def param(key: String)(value: Any) = new DashboardBuilder(consumer, token, params + (key -> value.toString))
    def product = Tumblr.user / "dashboard" << params <@ (consumer, token) ># ('response ! ('posts ? (list ! obj)))
    val limit = param("limit")_
    val offset = param("offset")_
    val posttype = param("type")_
    val since_id = param("since_id")_
    val reblog_info = param("reblog_info")_
    val notes_info = param("notes_info")_
  }
}

object Post extends Js {
  val title = 'title ? str
  val timestamp = 'timestamp ? num
}
