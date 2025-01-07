FROM eclipse-temurin:17-jre-alpine
ADD target/*.jar /home/madapi-mtn-customer-transfer-aggregator.jar
CMD ["java", "-jar", "/home/madapi-mtn-customer-transfer-aggregator.jar"]