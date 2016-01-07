/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rhttpc.client

import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import rhttpc.transport._
import rhttpc.client.protocol.Correlated

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.language.postfixOps
import scala.util.{Success, Try}

class MockTransport(awaitCond: (() => Boolean) => Unit)(implicit ec: ExecutionContext)
  extends PubSubTransport[Correlated[String], AnyRef] {

  @volatile private var _publicationPromise: Promise[Unit] = _
  @volatile var replySubscriptionPromise: Promise[String] = _
  @volatile var ackOnReplySubscriptionFuture: Future[Any] = _
  @volatile private var consumer: ActorRef = _

  def publicationPromise: Promise[Unit] = {
    awaitCond(() => _publicationPromise != null)
    _publicationPromise
  }

  override def publisher(data: OutboundQueueData): Publisher[Correlated[String]] = new Publisher[Correlated[String]] with MockSerializer {
    override def publish(request: Message[Correlated[String]]): Future[Unit] = {
      _publicationPromise = Promise[Unit]()
      replySubscriptionPromise = Promise[String]()
      implicit val timeout = Timeout(5 seconds)
      replySubscriptionPromise.future.onComplete { result =>
        ackOnReplySubscriptionFuture = consumer ? Correlated(result, request.content.correlationId)
      }
      _publicationPromise.future
    }

    override def close(): Unit = {}

  }

  override def subscriber(data: InboundQueueData, consumer: ActorRef): Subscriber[AnyRef] = new Subscriber[AnyRef] with MockDeserializer {
    MockTransport.this.consumer = consumer

    override def run(): Unit = {}

    override def stop(): Unit = {}
  }

  private trait MockSerializer extends Serializer[Correlated[String]] {
    override def serialize(obj: Correlated[String]): String = obj.msg
  }

  private trait MockDeserializer extends Deserializer[AnyRef] {
    override def deserialize(value: String): Try[AnyRef] = Success(value)
  }

}

object MockInboundQueueData extends InboundQueueData("fooInbound", batchSize = 11)

object MockOutboundQueueData extends OutboundQueueData("fooOutbound")