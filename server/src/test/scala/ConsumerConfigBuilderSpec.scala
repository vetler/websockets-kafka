import org.scalatest.{FlatSpec, Matchers}

class ConsumerConfigBuilderSpec extends FlatSpec with Matchers {

  "Not setting required arguments" should "throw an exception" in {
    val builder = new ConsumerConfigBuilder
    builder += (("group.id", "test-group-id"))

    a[IllegalArgumentException] should be thrownBy {
      builder.result
    }
  }

  "Setting all required arguments" should "create a ConsumerConfig" in {
    val builder = new ConsumerConfigBuilder
    builder += (("group.id", "some-id"))
    builder += (("zookeeper.connect", "some-zookeeker"))
    builder.result should not be (null)
  }
}