#!groovy
/*
 * Copyright (C) 2017 by Sebastian Hasait (sebastian at hasait dot de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

properties([
		buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '3', daysToKeepStr: '', numToKeepStr: '5')),
		parameters([
				string(name: 'releaseVersion', defaultValue: '', description: 'Release version - if set a release will be build, otherwise normal CI'),
				string(name: 'developmentVersion', defaultValue: '', description: 'Next development version _without_ SNAPSHOT - if set POMs will be updated after build'),
				string(name: 'gitUserName', defaultValue: 'ciserver', description: 'Git user name'),
				string(name: 'gitUserEmail', defaultValue: 'ciserver@hasait.de', description: 'Git user email')
		]),
		pipelineTriggers([pollSCM('H/10 * * * *')])
])

node('linux') {
	echo "params.releaseVersion = ${params.releaseVersion}"
	echo "params.developmentVersion = ${params.developmentVersion}"
	echo "params.gitUserName = ${params.gitUserName}"
	echo "params.gitUserEmail = ${params.gitUserEmail}"

	def wsHome
	def jdkHome
	def jdk6Home
	def mvnHome
	def mvnRepo

	stage('Prepare') {
		wsHome = pwd()
		echo "wsHome = ${wsHome}"

		jdkHome = tool 'JDK8'
		echo "jdkHome = ${jdkHome}"

		jdk6Home = tool 'JDK6'
		echo "jdk6Home = ${jdk6Home}"

		mvnHome = tool 'M3'
		echo "mvnHome = ${mvnHome}"

		mvnRepo = "${wsHome}/.m2repo"
		echo "mvnRepo = ${mvnRepo}"
		sh "rm -rf ${mvnRepo}"
		sh "mkdir ${mvnRepo}"

		sh "rm -rf 'co'"
	}

	configFileProvider([configFile(fileId: 'ciserver-settings.xml', targetLocation: 'maven-settings.xml', variable: 'mvnSettings'), configFile(fileId: 'ciserver-toolchains.xml', targetLocation: 'maven-toolchains.xml', variable: 'mvnToolchains')]) {
		withEnv(["PATH+JDK=${jdkHome}/bin", "PATH+MVN=${mvnHome}/bin"]) {
			dir('co') {
				def mvnOptions = "-s ${mvnSettings} --global-toolchains ${mvnToolchains} -Dmaven.repo.local=${mvnRepo}"
				def mvnCommand = "mvn -B -U -e ${mvnOptions}"
				def currentBranch
				def releaseTag

				stage('Checkout') {
					checkout scm

					sh "printenv"
					sh "git config --list"
					currentBranch = sh(returnStdout: true, script: "git branch | grep \\* | cut -d ' ' -f2").trim()
					echo "\u27A1 On branch ${currentBranch}..."

					if (params.releaseVersion || params.developmentVersion) {
						echo "\u27A1 Configuring git..."
						sh "git config user.name '${params.gitUserName}'"
						sh "git config user.email '${params.gitUserEmail}'"
						sh "git config --local credential.username '${params.gitUserName}'"
					}

					if (params.releaseVersion) {
						releaseTag = "v${params.releaseVersion}"
						def msg = "Changing POM versions to release version ${params.releaseVersion}"
						echo "\u27A1 ${msg}..."
						sh "${mvnCommand} release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=${params.releaseVersion}-REMOVEME-SNAPSHOT"
						sh "find . -type f -name pom.xml -exec sed -i -e 's/${params.releaseVersion}-REMOVEME-SNAPSHOT/${params.releaseVersion}/' \\{\\} \\;"
						sh "find . -type f -name pom.xml -exec git add \\{\\} \\;"
						sh "git commit -m '[Jenkinsfile] ${msg}'"
						sh "git tag ${releaseTag}"
					}
				}

				stage('Deploy') {
					try {
						echo "\u27A1 Deploying project..."
						sh "${mvnCommand} deploy -Dmaven.test.failure.ignore=true"
					} catch (err) {
						echo "\u27A1 Error: ${err}"
						currentBuild.result = 'FAILURE'
						throw err
					}
					if (params.releaseVersion) {
						echo "\u27A1 Pushing release tag ${releaseTag}..."
						sh "git push -f origin tag ${releaseTag}"
					}
					if (params.developmentVersion) {
						def msg = "Changing POM versions to development version ${params.developmentVersion}"
						echo "\u27A1 ${msg}..."

						sh "${mvnCommand} release:update-versions -DautoVersionSubmodules=true -DdevelopmentVersion=${params.developmentVersion}-SNAPSHOT"
						sh "find . -type f -name pom.xml -exec git add \\{\\} \\;"
						sh "git commit -m '[Jenkinsfile] ${msg}'"
						sh "git push origin ${currentBranch}"
					}
				}

				stage('Collect Test Results') {
					junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
				}

				stage('Archive Artifacts') {
					archiveArtifacts artifacts: '**/target/*.jar', onlyIfSuccessful: true, allowEmptyArchive: true
				}
			}
		}
	}
}
