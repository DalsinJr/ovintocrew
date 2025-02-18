# Usa a imagem do OpenJDK 21 para rodar a aplicação
FROM openjdk:21-jdk-slim

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o JAR gerado pela aplicação para dentro do container
COPY target/ovintocrew-0.0.1-SNAPSHOT.jar ovintocrew.jar

# Expõe a porta que a aplicação utilizará
EXPOSE 8080

# Define o comando para rodar a aplicação
CMD ["java", "-jar", "ovintocrew.jar"]