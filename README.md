<img src="ttps://vkceyugu.cdn.bspapp.com/VKCEYUGU-imgbed/8f959db3-8915-4a79-ac7c-079ab66edaf2.png" alt="MiniTool Logo" width="50%">

# Mini Tool: 称手的小工具集合

[![License](https://img.shields.io/github/license/bytejojo/minitool)](https://opensource.org/licenses/mit/)
[![Gitter](https://badges.gitter.im/bytejojo/minitool.svg)](https://gitter.im/bytejojo/minitool)

## Introduction

As distributed systems become increasingly popular, the reliability between services is becoming more important than ever before.
minitool takes "flow" as breakthrough point, and works on multiple fields including **flow control**,
**traffic shaping**, **circuit breaking** and **system adaptive protection**, to guarantee reliability and resilience for microservices.

minitool has the following features:

- **Rich applicable scenarios**: minitool has been wildly used in Alibaba, and has covered almost all the core-scenarios in Double-11 (11.11) Shopping Festivals in the past 10 years, such as “Second Kill” which needs to limit burst flow traffic to meet the system capacity, message peak clipping and valley fills, circuit breaking for unreliable downstream services, cluster flow control, etc.
- **Real-time monitoring**: minitool also provides real-time monitoring ability. You can see the runtime information of a single machine in real-time, and the aggregated runtime info of a cluster with less than 500 nodes.
- **Widespread open-source ecosystem**: minitool provides out-of-box integrations with commonly-used frameworks and libraries such as Spring Cloud, Dubbo and gRPC. You can easily use minitool by simply add the adapter dependency to your services.
- **Polyglot support**: minitool has provided native support for Java, [Go](https://github.com/bytejojo/minitool-golang) and [C++](https://github.com/bytejojo/minitool-cpp).
- **Various SPI extensions**: minitool provides easy-to-use SPI extension interfaces that allow you to quickly customize your logic, for example, custom rule management, adapting data sources, and so on.

Features overview:

![features-of-minitool](./doc/image/minitool-features-overview-en.png)

## Documentation

See the [中文文档](https://github.com/bytejojo/minitool/wiki/%E4%BB%8B%E7%BB%8D) for document in Chinese.

See the [Wiki](https://github.com/bytejojo/minitool/wiki) for full documentation, examples, blog posts, operational details and other information.

minitool provides integration modules for various open-source frameworks
(e.g. Spring Cloud, Apache Dubbo, gRPC, Spring WebFlux, Reactor) and service mesh.
You can refer to [the document](https://github.com/bytejojo/minitool/wiki/Adapters-to-Popular-Framework) for more information.

If you are using minitool, please [**leave a comment here**](https://github.com/bytejojo/minitool/issues/18) to tell us your scenario to make minitool better.
It's also encouraged to add the link of your blog post, tutorial, demo or customized components to [**Awesome minitool**](./doc/awesome-minitool.md).

## Ecosystem Landscape

![ecosystem-landscape](./doc/image/minitool-opensource-eco-landscape-en.png)

## Quick Start

Below is a simple demo that guides new users to use minitool in just 3 steps. It also shows how to monitor this demo using the dashboard.

### 1. Add Dependency

**Note:** minitool Core requires Java 7 or later.

If your're using Maven, just add the following dependency in `pom.xml`.

```xml
<!-- replace here with the latest version -->
<dependency>
    <groupId>com.bytejojo.csp</groupId>
    <artifactId>minitool-core</artifactId>
    <version>1.8.0</version>
</dependency>
```

If not, you can download JAR in [Maven Center Repository](https://mvnrepository.com/artifact/com.bytejojo.csp/minitool-core).

### 2. Define Resource

Wrap your code snippet via minitool API: `SphU.entry(resourceName)`.
In below example, it is `System.out.println("hello world");`:

```java
try (Entry entry = SphU.entry("HelloWorld")) {
    // Your business logic here.
    System.out.println("hello world");
} catch (BlockException e) {
    // Handle rejected request.
    e.printStackTrace();
}
// try-with-resources auto exit
```

So far the code modification is done. We've also provided [annotation support module](https://github.com/bytejojo/minitool/blob/master/minitool-extension/minitool-annotation-aspectj/README.md) to define resource easier.

### 3. Define Rules

If we want to limit the access times of the resource, we can **set rules to the resource**.
The following code defines a rule that limits access to the resource to 20 times per second at the maximum.

```java
List<FlowRule> rules = new ArrayList<>();
FlowRule rule = new FlowRule();
rule.setResource("HelloWorld");
// set limit qps to 20
rule.setCount(20);
rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
rules.add(rule);
FlowRuleManager.loadRules(rules);
```

For more information, please refer to [How To Use](https://github.com/bytejojo/minitool/wiki/How-to-Use).


### 5. Start Dashboard

> Note: Java 8 is required for building or running the dashboard.

minitool also provides a simple dashboard application, on which you can monitor the clients and configure the rules in real time.

![dashboard](https://user-images.githubusercontent.com/9434884/55449295-84866d80-55fd-11e9-94e5-d3441f4a2b63.png)

For details please refer to [Dashboard](https://github.com/bytejojo/minitool/wiki/Dashboard).

## Trouble Shooting and Logs

minitool will generate logs for troubleshooting and real-time monitoring.
All the information can be found in [logs](https://github.com/bytejojo/minitool/wiki/Logs).

## Bugs and Feedback

For bug report, questions and discussions please submit [GitHub Issues](https://github.com/bytejojo/minitool/issues).

Contact us via [Gitter](https://gitter.im/bytejojo/minitool) or [Email](mailto:bytejojo@163.com).

## Contributing

Contributions are always welcomed! Please refer to [CONTRIBUTING](./CONTRIBUTING.md) for detailed guidelines.

You can start with the issues labeled with [`good first issue`](https://github.com/bytejojo/minitool/issues?q=is%3Aissue+is%3Aopen+label%3A%22good+first+issue%22).

## Credits

Thanks [Guava](https://github.com/google/guava), which provides some inspiration on rate limiting.

And thanks for all [contributors](https://github.com/bytejojo/minitool/graphs/contributors) of minitool!

## Who is using

These are only part of the companies using minitool, for reference only.
If you are using minitool, please [add your company here](https://github.com/bytejojo/minitool/issues/18) to tell us your scenario to make minitool better :)

[![Bytejojo简书](https://www.jianshu.com/p/cdba184baea5)](https://www.jianshu.com/p/cdba184baea5)


