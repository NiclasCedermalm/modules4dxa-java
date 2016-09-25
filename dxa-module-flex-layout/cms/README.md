Install CMS modules and example content
=========================================

There are a number of CMS packages that can be installed in SDL Web 8.
The following packages are provided:

* FlexLayout-Module_v1.0.1.zip - FlexLayout CMS module with needed schemas and templates
* FlexLayout-ContentTypes_v1.0.1.zip - XPM Content types for various container components 

Instructions
-------------

Either SDL Web Content Porter 8 can be used to import the above packages, or the provided PowerShell script can be used.

#### Instructions for using the PowerShell script:

Before running the import script the needed DLLs needs to be copied. See [Import/Export DLLs](./ImportExport/README.md) for further information.

The CMS import script is generic and is used for all packages. The syntax for calling the script:

```
.\cms-import.ps1  -cmsUrl [CMS url] -moduleZip [Module ZIP filename]
```

To setup CMS data the packages needs to be imported in the following order:

1. Setup modules: `.\cms-import.ps1 -moduleZip FlexLayout-Module-v1.0.1.zip`
2. Setup content types: `.\cms-import.ps1 -moduleZip FlexLayout-ContentTypes-v1.0.1.zip`

    



