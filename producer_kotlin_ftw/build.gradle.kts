import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot")
	id("io.spring.dependency-management") version "1.0.7.RELEASE"
	id("spring-cloud-contract")
	id("maven-publish")
	kotlin("jvm") version "1.3.41"
	kotlin("plugin.spring") version "1.3.41"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

// tag::deps_repos[]
repositories {
	mavenCentral()
	mavenLocal()
	maven { url = uri("https://repo.spring.io/release") }
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}
// end::deps_repos[]

// tag::dep_mgmt[]
dependencyManagement {

	val BOM_VERSION: String by project

	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:$BOM_VERSION")
	}
}
// end::dep_mgmt[]

// tag::deps[]
dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.cloud:spring-cloud-contract-gradle-plugin")
	testImplementation("org.springframework.cloud:spring-cloud-starter-contract-verifier")
	testImplementation("io.rest-assured:rest-assured")
	testImplementation("io.rest-assured:spring-mock-mvc")
	testImplementation("io.rest-assured:spring-web-test-client") {
		exclude(group = "org.springframework", module = "spring-context")
		exclude(group = "org.springframework", module = "spring-webflux")
	}
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
		exclude(group = "junit", module = "junit")
	}
}
// end::deps[]

// tag::contract_dsl[]
contracts {
	packageWithBaseClasses.set("com.example.fraud")
}
// end::contract_dsl[]

tasks.withType<Delete> {
	doFirst {
		delete("~/.m2/repository/com/example/producer-kotlin-ftw-gradle")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	systemProperty("spring.profiles.active", "gradle")
	testLogging {
		exceptionFormat = TestExceptionFormat.FULL
	}
	afterSuite(KotlinClosure2({ desc: TestDescriptor, result: TestResult ->
		if (desc.parent == null) {
			if (result.testCount == 0L) {
				throw IllegalStateException("No tests were found. Failing the build")
			}
			else {
				println("Results: (${result.testCount} tests, ${result.successfulTestCount} successes, ${result.failedTestCount} failures, ${result.skippedTestCount} skipped)")
			}
		} else { /* Nothing to do here */ }
	}))
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}