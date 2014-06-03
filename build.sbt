name := "MultirExperiments"

version := "0.1"

scalaVersion := "2.10.1"

fork := true

javaOptions in run += "-Xmx12G"

javaOptions in run += "-Djava.util.Arrays.useLegacyMergeSort=true"

libraryDependencies ++= Seq(
  "edu.washington.cs.knowitall" % "multir-framework_2.10" % "0.3-SNAPSHOT" withSources() withJavadoc(),
  "edu.washington.cs.knowitall.stanford-corenlp" % "stanford-ner-models" % "1.3.5",
  "edu.washington.cs.knowitall.stanford-corenlp" % "stanford-postag-models" % "1.3.5",
  "edu.washington.cs.knowitall.stanford-corenlp" % "stanford-dcoref-models" % "1.3.5",
  "edu.washington.cs.knowitall.stanford-corenlp" % "stanford-parse-models" % "1.3.5",
  "edu.washington.cs.knowitall.stanford-corenlp" % "stanford-sutime-models" % "1.3.5",
  "edu.washington.cs.knowitall" % "reverb-core" % "1.4.3",
  "edu.washington.cs.knowitall.nlptools" % "nlptools-core_2.10" % "2.4.4",
  "edu.washington.cs.knowitall.nlptools" % "nlptools-chunk-opennlp_2.10" % "2.4.4",
  "edu.mit" % "jwi" % "2.2.3",
  "postgresql" % "postgresql" % "9.0-801.jdbc4",
  "edu.washington.cs.knowitall.nlptools" % "nlptools-wordnet-uw_2.10" % "2.4.4",
  "org.apache.hadoop" % "hadoop-core" % "0.20.2",
  "com.cedarsoftware" % "json-io" % "2.6.0",
  "com.google.code.externalsortinginjava" % "externalsortinginjava" % "0.1.9",
  "edu.washington.cs.knowitall.taggers" % "taggers-core_2.10" % "0.4" excludeAll(ExclusionRule(organization = "com.googlecode.clearnlp")))


resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"


EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
