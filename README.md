![alt text](oskari_logo_rgb_horizontal.svg "Oskari logo")

# Oskari-server

[Oskari](https://www.oskari.org/) aims to provide a framework and a collection of server modules for rapid development of feature-rich web map applications.

Documentation available at [https://www.oskari.org].

This repository contains Maven modules that can be used as dependencies when creating an Oskari-based server-side application and are served from (non-browseable) Maven repositories at:
- Released versions (`master`-branch): `https://oskari.org/repository/maven/releases/`
- Nightly builds (`develop`-branch): `https://oskari.org/repository/maven/snapshots/`

Our template for server-side application is located on [https://github.com/oskariorg/sample-server-extension]. The template is the preferred way of customizing Oskari-based applications.

# Reporting issues

All Oskari-related issues should be reported here: https://github.com/oskariorg/oskari-documentation/issues

# Contributing

Please read the [contribution guidelines](https://oskari.org/contribute) before contributing pull requests to the Oskari project.

## Maven targets

Build with:

    mvn clean install

## Prerequisites

Oskari-server has been tested to compile using:

- JDK 17
- Apache Maven 3.6.3

## License

This work is dual-licensed under MIT and [EUPL v1.1](https://joinup.ec.europa.eu/software/page/eupl/licence-eupl)
(any language version applies, English version is included in https://github.com/oskariorg/oskari-docs/blob/master/documents/LICENSE-EUPL.pdf).
You can choose between one of them if you use this work.

`SPDX-License-Identifier: MIT OR EUPL-1.1`

Copyright (c) 2014-present National Land Survey of Finland
