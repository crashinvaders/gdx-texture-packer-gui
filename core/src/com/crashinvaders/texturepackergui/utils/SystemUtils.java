package com.crashinvaders.texturepackergui.utils;

public class SystemUtils {

    public static final OperatingSystem OPERATING_SYSTEM;
    public static final CpuArch CPU_ARCH;

    static {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            OPERATING_SYSTEM = OperatingSystem.Windows;
        } else if (osName.contains("mac")) {
            OPERATING_SYSTEM = OperatingSystem.MacOS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            OPERATING_SYSTEM = OperatingSystem.Linux;
        } else {
            OPERATING_SYSTEM = OperatingSystem.Other;
        }
    }

    static {
        String osArch = System.getProperty("os.arch");
        if (osArch.contains("x86_32")) {
            CPU_ARCH = CpuArch.X86;
        } else if (osArch.contains("amd64") || osArch.contains("x86_64")) {
            CPU_ARCH = CpuArch.Amd64;
        } else if (osArch.contains("arm64") || osArch.contains("aarch64")) {
            CPU_ARCH = CpuArch.Arm64;
        } else if (osArch.contains("arm")) {
            CPU_ARCH = CpuArch.Arm32;
        } else {
            CPU_ARCH = CpuArch.Other;
        }
    }

    static {
        System.out.println("[SystemUtils] Recognized system configuration is " + getPrintString());
    }

    public static boolean check(OperatingSystem os) {
        return OPERATING_SYSTEM == os;
    }

    public static boolean check(OperatingSystem os, CpuArch arch) {
        return OPERATING_SYSTEM == os && CPU_ARCH == arch;
    }

    public static boolean check(OperatingSystem os, CpuArch arch0, CpuArch arch1) {
        return OPERATING_SYSTEM == os && (CPU_ARCH == arch0 || CPU_ARCH == arch1);
    }

    public static boolean check(OperatingSystem os, CpuArch arch0, CpuArch arch1, CpuArch arch2) {
        return OPERATING_SYSTEM == os && (CPU_ARCH == arch0 || CPU_ARCH == arch1 || CPU_ARCH == arch2);
    }

    public static String getPrintString() {
        return OPERATING_SYSTEM.printName + " (" + CPU_ARCH.printName + ")";
    }

    public enum OperatingSystem {
        Windows("Windows"),
        Linux("Linux"),
        MacOS("MacOS"),
        Other("Unknown"),
        ;

        public final String printName;

        OperatingSystem(String printName) {
            this.printName = printName;
        }
    }

    public enum CpuArch {
        X86("x86"),
        Amd64("x64"),
        Arm32("arm32"),
        Arm64("arm64"),
        Other("Other");

        public final String printName;

        CpuArch(String printName) {
            this.printName = printName;
        }
    }
}
