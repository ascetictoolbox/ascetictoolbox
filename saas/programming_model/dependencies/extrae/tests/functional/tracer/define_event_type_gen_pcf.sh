#!/bin/bash

rm -fr *.sym *.mpits set-0

EXTRAE_ON=1 ./define_event_type_gen_pcf
../../../src/merger/mpi2prv -f TRACE.mpits -e .libs/define_event_type_gen_pcf -o define_event_type_gen_pcf.prv

# Actual comparison
diff define_event_type_gen_pcf.reference define_event_type_gen_pcf.pcf
