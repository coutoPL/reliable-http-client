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
package rhttpc.akkahttp.proxy

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import rhttpc.client.proxy.BackoffRetry

import java.time.{Duration => JDuration}
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object ProxyApp extends App {
  implicit val actorSystem = ActorSystem("rhttpc-proxy")
  import actorSystem.dispatcher
  implicit val materializer = ActorMaterializer()

  val retryStrategy = BackoffRetry(JDuration.ofSeconds(5), 1.0, 3)
  val proxy = Await.result(ReliableHttpProxy(failureHandleStrategyChooser = retryStrategy), 20 seconds)

  Runtime.getRuntime.addShutdownHook(new Thread {
    override def run(): Unit = {
      Await.result(proxy.close(), 5 minutes)
    }
  })

  proxy.run()
}