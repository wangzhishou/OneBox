#include <jni.h>
#include <string>

// This is a dummy file to force Gradle to build a native library
// and include the correct version of libc++_shared.so.
// The std::string usage ensures libc++ is actually linked into this library.

static volatile std::string g_dummyLinkerHelper = "dummy";

