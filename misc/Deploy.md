# Deploy steps

```bash
sbt publish
# ignore: Settings logger used after project was loaded.
cd dockerContainer
sbt kata:dockerBuildAndPush
cd dockerContainerBundle
sbt kata:dockerBuildAndPush
```

