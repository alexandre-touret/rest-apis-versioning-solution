#!/bin/sh

docker run --rm -v $(pwd)/vale.sh/styles:/styles --rm -v $(pwd):/docs -w /docs jdkato/vale content/ 