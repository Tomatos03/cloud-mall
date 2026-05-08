# Build & Run Commands

```bash
# Full build
mvn clean compile -DskipTests
mvn clean package

# Single module build
mvn clean compile -DskipTests -pl online-shop-framework
mvn clean compile -DskipTests -pl online-shop-web

# Run aggregation (recommended for local dev, port 7777)
mvn -pl online-shop-aggregation spring-boot:run

# Run individual modules
mvn -pl online-shop-manager spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl online-shop-web spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl online-shop-merchant spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl im spring-boot:run

# Tests
mvn test
mvn test -pl online-shop-framework
mvn test -Dtest=SeckillActivityServiceImplTest
mvn test -Dtest=SeckillActivityServiceImplTest#testCreateActivity
```
