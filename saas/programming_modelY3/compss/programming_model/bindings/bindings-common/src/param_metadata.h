#ifndef PARAM_METADATA_H
#define PARAM_METADATA_H

// MATCHES JAVA COMPSsRuntime API Enum
// Adds internal data representations
enum datatype {
        boolean_dt = 0,
        char_dt, 
        byte_dt, 
        short_dt, 
        int_dt,
        long_dt,
        float_dt,
        double_dt,
        string_dt,
        file_dt,
        object_dt,
        persistent_dt,
	sco_dt,

	wchar_dt,
	wstring_dt,
	longlong_dt,
	void_dt,
	any_dt,
	null_dt,
};

// MATCHES JAVA COMPSsRuntime API Enum
// Adds internal data representations
enum direction {
	in_dir = 0, 
	out_dir, 
	inout_dir, 

	null_dir,
};

#endif /* PARAM_METADATA_H */
