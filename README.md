
[![SORMAS - Surveillance, Outbreak Response Management and Analysis System](logo.png)](https://sormas.org)

![License](https://img.shields.io/badge/license-GPL%20v3-blue)
![Latest Release](https://img.shields.io/github/v/release/hzi-braunschweig/SORMAS-Project)
[![SORMAS BUILD ALL](https://github.com/Soontao/SORMAS-Project/workflows/SORMAS%20BUILD%20ALL/badge.svg?branch=development)](https://github.com/Soontao/SORMAS-Project/actions?query=workflow:"SORMAS+BUILD+ALL")

**SORMAS** (Surveillance Outbreak Response Management and Analysis System) is an open source eHealth system - consisting of separate web and mobile apps - that is geared towards optimizing the processes used in monitoring the spread of infectious diseases and responding to outbreak situations.

#### How Does it Work?
You can give SORMAS a try on our play server at https://sormas.helmholtz-hzi.de!

#### How Can I Get Involved?
Have a look at our [*Contributing Readme*](CONTRIBUTING.md) and contact us at sormas@helmholtz-hzi.de to learn how you can help to drive the development of SORMAS forward. SORMAS is a community-driven project, and we'd love to have you on board!

#### How Can I Report a Bug or Request a Feature?
Please [create a new issue](https://github.com/hzi-braunschweig/SORMAS-Project/issues/new/choose) and read the [*Submitting an Issue*](CONTRIBUTING.md#submitting-an-issue) guide for more detailed instructions. We appreciate your help!

<p align="center"><img src="https://user-images.githubusercontent.com/23701005/74659600-ebb8fc00-5194-11ea-836b-a7ca9d682301.png"/></p>

## Project Structure
The project consists of the following modules:

- **sormas-api:** General business logic and definitions for data exchange between app and server
- **sormas-app:** The Android app
- **sormas-backend:** Server entity services, facades, etc.
- **sormas-base:** Base project that also contains build scripts
- **sormas-ear:** The ear needed to build the application
- **sormas-rest:** The REST interface
- **sormas-ui:** The web application

## Server Management

* [Setting up a SORMAS server](SERVER_SETUP.md)
* [Updating a SORMAS server](SERVER_UPDATE.md)
