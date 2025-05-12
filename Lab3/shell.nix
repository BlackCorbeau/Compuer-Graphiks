{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
    buildInputs = [
        pkgs.jdk21
        pkgs.gradle
        pkgs.libGL
        pkgs.glfw
        pkgs.xorg.libX11
        pkgs.xorg.libXcursor
        pkgs.xorg.libXrandr
        pkgs.xorg.libXi
        pkgs.mesa
    ];

    shellHook = ''
        export JAVA_HOME="${pkgs.jdk21}"
        export PATH="${pkgs.gradle}/bin:$PATH"
        export LD_LIBRARY_PATH="${
            pkgs.lib.makeLibraryPath [
                pkgs.libGL
                pkgs.glfw
                pkgs.xorg.libX11
                pkgs.xorg.libXcursor
                pkgs.xorg.libXrandr
                pkgs.xorg.libXi
                pkgs.mesa
            ]
        }:$LD_LIBRARY_PATH"
    '';
}

