# organizations
The organizations repository is a DAPM demo project and  contains organizations each runnining in a Spring Boot application.

## Running the demo
You must have [docker-desktop](https://www.docker.com/products/docker-desktop/) application open to start the containers.

There is a `compose.yaml` file in each organization which has to be running.
```
docker-compose up -d
```
Instead of starting each docker-compose independently, there is a `run.sh` script for easy starting of all the docker containers, again run this with docker-dektop open.
```
./run.sh
```

Once the docker-containers are running, start the **orgA** and **orgB** Spring Boot applications before running **orgC**. In this demo, **orgC** is the organization that builds the pipeline from its main method (automatically) and the other two organizations store processing element templates which are used to build the pipeline.

To change the pipeline **orgC** is building, change the string path to another json pipeline representation in the main method.
```java
try {
    contents = Files.readString(Paths.get("orgC/src/main/resources/simple_pipeline.json"));
} catch (IOException e) {
    throw new RuntimeException(e);
}
```

To run pipelines such as *heuristics_miner_pipeline.json* and *concrete_pipeline.json*, add algorithm JARS locally to the *orgB/templates/algorithm* folder. These JARS can be downloaded from [Gitlab](https://lab.compute.dtu.dk/dapm-thesis/organizations/-/tree/main_with_jars/orgB/src/main/java/templates/algorithm?ref_type=heads).
