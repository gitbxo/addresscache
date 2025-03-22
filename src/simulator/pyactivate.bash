#!/bin/bash

SRCDIR=`pwd`
PYENVDIR=`dirname ${SRCDIR}`/.pyenv.address

# echo "SRCDIR = ${SRCDIR}"
echo "PYENVDIR = ${PYENVDIR}"

test -d "${PYENVDIR}" || python3 -m venv "${PYENVDIR}"

test '!' -z "${VIRTUAL_ENV}" ||
  source "${PYENVDIR}/bin/activate"

test '!' -z "${VIRTUAL_ENV}" ||
  test '!' -f requirements.txt ||
  python3 -m pip install -r requirements.txt


