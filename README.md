# Mailer
This is a simple command-line mailer written in Java.

## Build this project

```
mvn clean package
```

## Examples

## Send mail to joe@gmail.com using mailer.conf

### Prerequisites
The following contents should be defined in `./conf/mailer.conf`:
```
mail.smtp.host=smtp.gmail.com
mail.smtp.port=465
mail.smtp.auth=true
mail.smtp.ssl.enable=true
mail.smtp.starttls.enable=false
```

Run:
```
java -jar ./target/mailer-jar-with-dependencies.jar -t joe@gmail.com -s "a little test" -b "test email body" -f joe@gmail.com
```

## Send mail to joe@gmail.com with custom *.conf

### Prerequisites
The following contents should be defined in `./mymailer.conf`:
```
mail.smtp.host=smtp.gmail.com
mail.smtp.port=465
mail.smtp.auth=true
mail.smtp.ssl.enable=true
mail.smtp.starttls.enable=false
```

Run:
```
java -jar ./target/mailer-jar-with-dependencies.jar -t joe@gmail.com -s "a little test" -b "test email body" -f joe@gmail.com -c ./mymailer.conf
```

## Installation

To install, simply add this artifact as a dependency to your existing Maven project. Add the following to your project's pom.xml:
```
<project>
    ...
    <dependencies>
        ...
        <dependency>
            <groupId>com.lyders.mailer</groupId>
            <artifactId>mailer</artifactId>
            <version>1.1.0</version>
        </dependency>
        ...
    </dependencies>
    ...
</project>
``` 

## Deploy to Apache Maven (i.e., "Maven Central") via Sonatype.org

from: https://central.sonatype.org/pages/apache-maven.html

### Upload release to Sonatype staging repo. 

NOTES: 
* To avoid slowing down every build, I moved the javadoc, gpg, and source plugins under the release profile.
* I wanted to be able to validate the deployment in staging repo first, then deploy it. So, I set <autoReleaseAfterClose> to false. 
```
mvn clean deploy -P release
```

With the property autoReleaseAfterClose set to false you can manually inspect the staging repository in the Nexus Repository Manager and trigger a release of the staging repository later with
```
mvn nexus-staging:release
```

If you find something went wrong you can drop the staging repository with
```
mvn nexus-staging:drop
```

Please read Build Promotion with the Nexus Staging Suite in the book Repository Management with Nexus for more information about the Nexus Staging Maven Plugin.

... *OR* ...
  
If your version is a release version (does not end in -SNAPSHOT) and with this setup in place, you can run a deployment to OSSRH and an automated release to the Central Repository with the usual:

```
mvn release:clean release:prepare
```

by answering the prompts for versions and tags, followed by
```
mvn release:perform
```

This execution will deploy to OSSRH and release to the Central Repository in one go, thanks to the usage of the Nexus Staging Maven Plugin with autoReleaseAfterClose set to true.

## Built With

 * [Maven 3.6.3](https://maven.apache.org/) - Dependency Management
 * [Amazon Corretto Java 11.0.4](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html) 
 * [junit-jupiter 5.6.0](pom.xml)
 * IntelliJ IDEA Community Edition 2021.3
 * Visual Studio Code 1.42.1
 * Windows 10 Pro version 1093

## Contributing

If you find any bugs, issues, concerns or have advice on how to improve this project, please contact me at richard@lyders.com. 

## Authors

* **Richard Lyders** - [RichardLyders.com](http://richardlyders.com/)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Acknowledgments

* My little buddy Oliver, who makes each day a joy.
* Tony Benbrahim, sharing his decades of development experience
