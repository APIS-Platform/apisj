# Welcome to JApis



# About
JApis is a pure-Java implementation of the Ethereum protocol. For high-level information about Ethereum and its goals, visit [ethereum.org](https://ethereum.org). The [ethereum white paper](https://github.com/ethereum/wiki/wiki/White-Paper) provides a complete conceptual overview, and the [yellow paper](http://gavwood.com/Paper.pdf) provides a formal definition of the protocol.

# Running JApis

##### Adding as a dependency to your Maven project: 

```
   <dependency>
     <groupId>org.ethereum</groupId>
     <artifactId>ethereumj-core</artifactId>
     <version>1.5.0-RELEASE</version>
   </dependency>
```

##### or your Gradle project: 

```
   repositories {
       mavenCentral()
   }
   compile "org.ethereum:ethereumj-core:1.5.+"
```

As a starting point for your own project take a look at https://github.com/ether-camp/ethereumj.starter

##### Building an executable JAR
```
git clone https://github.com/ethereum/ethereumj
cd ethereumj
cp ethereumj-core/src/main/resources/ethereumj.conf ethereumj-core/src/main/resources/user.conf
vim ethereumj-core/src/main/resources/user.conf # adjust user.conf to your needs
./gradlew clean shadowJar
java -jar ethereumj-core/build/libs/ethereumj-core-*-all.jar
```


# License
JApis is released under the [LGPL-V3 license](LICENSE).

