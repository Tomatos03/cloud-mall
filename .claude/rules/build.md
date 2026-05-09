# Build & Run Commands

```bash
# Full build
mvn clean compile -DskipTests
mvn clean package

# Single module build
mvn clean compile -DskipTests -pl cloud-mall-framework
mvn clean compile -DskipTests -pl cloud-mall-web

# Run aggregation (recommended for local dev, port 7777)
mvn -pl cloud-mall-aggregation spring-boot:run

# Run individual modules
mvn -pl cloud-mall-manager spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl cloud-mall-web spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl cloud-mall-merchant spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl im spring-boot:run

# Tests
mvn test
mvn test -pl cloud-mall-framework
mvn test -Dtest=SeckillActivityServiceImplTest
mvn test -Dtest=SeckillActivityServiceImplTest#testCreateActivity
```
