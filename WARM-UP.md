# WARM UP / Preparation

If you want to avoid wasting your time against a shared WIFI local connection during the first steps or your workshops, do these ones before:

## If you want to run the workshop on your desktop
### Tools to install

1. Check [this documentation](https://github.com/alexandre-touret/rest-apis-versioning-workshop#traffic_light-prerequisites) and install [the required tools](https://github.com/alexandre-touret/rest-apis-versioning-workshop#wrench-tools) first
2. Check the release with the commands described in [the documentation](https://github.com/alexandre-touret/rest-apis-versioning-workshop#wrench-tools)

### Docker infrastructure warmup

Now you can start for the first time your Docker infrastructure, and download all the required layers:

```jshelllanguage
cd infrastructure  
docker compose up
```

Stop then by typing ``CTRL+C``.

### Java dependencies download

Run the following command at the root of your project:

```jshelllanguage
./gradlew build -x test
```

Now you aare ready!

## If you want to run it on Gitpod

Check [this documentation first](https://github.com/alexandre-touret/rest-apis-versioning-workshop#rocket-if-you-dont-want-to-bother-with-a-local-setup)