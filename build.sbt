lazy val baseSettings: Seq[Setting[_]] = Seq(
  scalaVersion       := "2.12.8",
  scalacOptions     ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:higherKinds",
    "-language:implicitConversions", "-language:existentials",
    "-unchecked",
    "-Yno-adapted-args",
    "-Ypartial-unification",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard",
    "-Xfuture"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
  resolvers += Resolver.sonatypeRepo("releases")
)

lazy val typelevel2019 = project.in(file("."))
  .settings(moduleName := "typelevel2019")
  .settings(baseSettings: _*)
  .aggregate(core, slides)
  .dependsOn(core, slides)

lazy val core = project
  .settings(moduleName := "typelevel2019-core")
  .settings(baseSettings: _*)
  .settings(
    libraryDependencies += "org.typelevel" %% "cats-core" % "1.6.0",
    libraryDependencies += "org.typelevel" %% "cats-effect" % "1.2.0",    
    libraryDependencies += "io.circe" %% "circe-generic" % "0.11.0",
    libraryDependencies += "org.http4s" %% "http4s-dsl" % "0.20.0-M7",
  )

lazy val slides = project
  .settings(moduleName := "typelevel2019-slides")
  .settings(baseSettings: _*)
  .settings(
    tutSourceDirectory := baseDirectory.value / "tut",
    tutTargetDirectory := baseDirectory.value / "../docs",
    watchSources ++= (tutSourceDirectory.value ** "*.html").get
  ).dependsOn(core)
  .enablePlugins(TutPlugin)
