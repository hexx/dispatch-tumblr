import org.specs2.mutable._

import com.github.hexx.dispatch.tumblr._

class TumblrSpec extends Specification {
  import dispatch.classic._
  import dispatch.classic.json.Js
  import dispatch.classic.json.JsHttp._
  import dispatch.classic.oauth.{ Consumer, Token }

  val consumer = Consumer("YOUR CONSUMER KEY", "YOUR CONSUMER SECRET")
  val username = "YOUR USERNAME"
  val password = "YOUR PASSWORD"

  trait HttpAfter extends After {
    lazy val http = new Http
    lazy val access_token_xauth = http(Auth.access_token(consumer, username, password))
    def after = http.shutdown()
  }

  "Blog Avatar" should {
    "pab-tech" in new HttpAfter {
      val req = Blog.avatar("pab-tech.tumblr.com")
      val res = http(req >:> (_("Content-Length")))
      res.head === "9001"
    }
  }
  "Blog Info" should {
    "pab-tech" in new HttpAfter {
      val res = http(Blog.info("pab-tech.tumblr.com", consumer))
      Blog.title(res) === "PAB@求職活動中"
    }
  }
  "Get Access Token via xAuth" should {
    "pab-tech" in new HttpAfter {
      access_token_xauth must beLike { case Token(_, _) => ok }
    }
  }
  "Blog Post" should {
    "pab-test" in new HttpAfter {
      // http(Blog.post("pab-test.tumblr.com", consumer, access_token_xauth, "type" -> "text", "title" -> "spec test title", "body" -> "spec test body"))
      success
    }
  }
  "User Dashboard" should {
    "pab-tech" in new HttpAfter {
      val res = http(User.dashboard(consumer, access_token_xauth))
      res.isEmpty must beFalse
      res map Post.timestamp forall (_.isInstanceOf[BigDecimal]) must beTrue
    }
  }
}
