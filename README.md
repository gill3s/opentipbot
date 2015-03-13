### Welcome to OpenTipBot

Opentipbot is an application giving any bitcoin-compatible crypto currencies a way to tip people on twitter. Opentipbot is based on Zetatip source code wich is a twitter tipbot for Zetacoin released by the end of 2014.

Opentipbot is a pure Java project using latest open source libraries and bitcoin-compatible JSON-RPC API.

For any questions about this project you can contact the lead developer gilles.cadignan@gmail.com

### Getting started

To get started, it is best to have the latest JDK and Maven installed.

#### Building from the command line

To perform a full build use
```
mvn clean install -P <Your target profile>
```

By default the target profile is "dev" so you can omit the -P paramter if you want to use the "dev" profile. The outputs of the build are under the `target` directory.

#### Building from an IDE

Alternatively, just import the project using your IDE. [IntelliJ](http://www.jetbrains.com/idea/download/) has Maven integration built-in and has a free Community Edition. Simply use `File | Import Project` and locate the `pom.xml` in the root of the cloned project source tree.

### Opentip Configuration

OPentip needs several components configuration. You will need to configure the database, the coin daemon and two twitter applications.

#### Database Configuration

Opentipbot needs a PosgreSQL database to operate. You will need to setup a database with a dedicated user and fill the appropriate info in `/opentipbot-persistence/profiles/`. This directory contains multiple profiles you can use for different environments (development, integration production etc). The source code provides two profiles but you can add more if you need to. For each profile there's one application.properties files containing all persistence configuration for PostgreSQL and Hibernate :

```
#Database Configuration
db.driver=org.postgresql.Driver
db.url=<Enter your jdbc URL here>
db.username=<Database Username>
db.password=<Database password>

#Hibernate Configuration
hibernate.dialect=org.hibernate.dialect.PostgreSQL82Dialect
hibernate.format_sql=true
hibernate.hbm2ddl.auto=update
hibernate.ejb.naming_strategy=org.hibernate.cfg.ImprovedNamingStrategy
hibernate.show_sql=false
hibernate.connection.autocommit=false
```

You will also need to create manually some table in the database. Execute the script /opentipbot-persistence/src/main/resources/create-user-connection.sql

#### Coin Daemon Configuration

Opentipbot will need a coin daemon to run in order to manage user wallets. For security reason I would recommend to host the coin deamon in a separate host and configure encrypt connexion and proper firewall configuration between the webapp/database servers and the coin daemon host. You will also need to configure RPC access to your daemon to make the communication possible. You also need to backup the wallet regularly, in order not to loose any coin.

Once your coin deamon is set up, you can configure opentipbot to acces it the proper way. Configuration file is located in `/opentipbot-persistence/profiles/`. This directory contains multiple profiles you can use for different environments (development, integration production etc). The source code provides two profiles but you can add more if you need to. For each profile there's one application.properties files containing the configuration of the coin daemon :

```
#coin daemon Configuration
# coin deamon url example : http://user:password@host:port
coind.url=<Put your coin daemon url here>
```
#### Twitter accounts Configuration
Opnetipbot will need a dedicated account on twitter to operate. First you will have to create the account and configure it as you will and then with this account create two apps.
The first app will be the notifier app, the one sending message to opentipbot users. You will need to generate four keys for it : the app key, the app secret, the access token and the access token secret. This first app needs read and write access right.

The second app is used only for the webapp, for this one you will only need the app key and the app secret. This second app only needs read access.

To set up twitter app and configure keys, please read carefully twitter dev documentation.

Once you've set all those keys you will need to put them in the configuration file in `/opentipbot-persistence/profiles/`. This directory contains multiple profiles you can use for different environments (development, integration production etc). The source code provides two profiles but you can add more if you need to. For each profile there's one application.properties files containing the configuration of twitter :

```
#Twitter configuration
opentipbot.notifier.twitter.appKey=<your notifier twitter app key>
opentipbot.notifier.twitter.appSecret=<your notifier twitter app secret>
opentipbot.notifier.twitter.accessToken=<your notifier twitter access token>
opentipbot.notifier.twitter.accessTokenSecret=<your notifier twitter access token secret>
opentipbot.webapp.twitter.appKey=<your webapp twitter app key>
opentipbot.webapp.twitter.appSecret=<your notifier twitter app secret>
```

### Running the bot

Opentipbot consists in one webapp for the website and on Java process running in background and managing incoming tweets.

#### Running the webapp
Once you have built the war file fil can deploy it on any application server. I recommend jetty for its simplicity but you can use Jboss or tomcat or any application server you want to host this web application.

#### Running the background jobs
The module called "opentipbot-jobs" once built generates a runnable jar. We provide a script for transforming this runnable jar in a background service for linux systems, you can use it if you want. Keep in mind that if that application is not running the bot wont work !

#### Got questions ?

Ask the main dev at opentipbot@gmail.com and get answers you ask for. Enjoy this tipbot !