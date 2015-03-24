# -*- coding: utf-8 -*-

# Copyright © 2014-2015 Cask Data, Inc.
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

#
# Cask common documentation build configuration file
#
# This file is execfile()d with the current directory set to its
# containing dir.
#
# Note that not all possible configuration values are present in this
# autogenerated file.
#
# All configuration values have a default; values that are commented out
# serve to show the default.

import sys
import os
import os.path
import subprocess
from datetime import datetime

# Cask Projects
# These can be set before this file is called, and control what is in effect

# Set:
#   CASK_PROJECT_TYPE
# to one these values:
CASK_PROJECT_TYPE_CDAP = 'cdap'
CASK_PROJECT_TYPE_CDAP_TUTORIAL = 'cdap-tutorial'
CASK_PROJECT_TYPE_COOPR = 'coopr'
CASK_PROJECT_TYPE_NONE = ''
# example: CASK_PROJECT_TYPE = 'cdap'

# If CASK_PROJECT_TYPE not defined, assume this is a CDAP project
try:
    CASK_PROJECT_TYPE
except NameError:
    CASK_PROJECT_TYPE = CASK_PROJECT_TYPE_CDAP

def get_sdk_version():
#     return '2.8.0', '2.8.0', '2.8.0'
    version = ''
    short_version = ''
    full_version = ''
    # Sets the Build Version
    grep_version_cmd = "grep '<version>' ../../../pom.xml | awk 'NR==1;START{print $1}'"
    try:
        full_version = subprocess.check_output(grep_version_cmd, shell=True).strip().replace("<version>", "").replace("</version>", "")
        version = full_version.replace("-SNAPSHOT", "")
        short_version = '%s.%s' % tuple(version.split('.')[0:2])
    except:
        print "Could not obtain version from pom.xml"
        pass
    return version, short_version, full_version

def print_sdk_version():
    version, short_version, full_version = get_sdk_version()
    if  version and version == full_version:
        print "SDK Version: %s" % version
    elif version and full_version: 
        print "SDK Version: %s (%s)" % (version, full_version)
    else:
        print "Could not get version (%s), full version (%s) from grep" % (version, full_version)

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#sys.path.insert(0, os.path.abspath('.'))

# -- General configuration ------------------------------------------------

if CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP:
    common_dir_redirect = '../../_common/'
else:
    common_dir_redirect = '../_common/'


# If your documentation needs a minimal Sphinx version, state it here.
#needs_sphinx = '1.0'

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = [
    'sphinx.ext.autodoc',
    'sphinxcontrib.googleanalytics',
    'sphinx.ext.todo',
    'sphinx.ext.pngmath',
    'sphinx.ext.ifconfig',
    'sphinx.ext.intersphinx',
]

intersphinx_mapping = {}

if CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP:
    intersphinx_mapping = {
      'admin':        ('../../admin-manual/',        os.path.abspath('../../admin-manual/build/html/objects.inv')),
      'developers':   ('../../developers-manual/',   os.path.abspath('../../developers-manual/build/html/objects.inv')),
      'integrations': ('../../integrations/', os.path.abspath('../../integrations/build/html/objects.inv')),
      'reference':    ('../../reference-manual',     os.path.abspath('../../reference-manual/build/html/objects.inv')),
      'examples':     ('../../examples-manual',      os.path.abspath('../../examples-manual/build/html/objects.inv')),
    }

# Add any paths that contain templates here, relative to this directory.
templates_path = ['_templates', common_dir_redirect + '_templates']

# The suffix of source filenames.
source_suffix = '.rst'

# The encoding of source files.
#source_encoding = 'utf-8-sig'

# The master toctree document.
master_doc = 'table-of-contents'

# General information about the project.
project = u''
if CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP:
    project = u'Cask Data Application Platform'

copyright = u'2014-%s Cask Data, Inc.' % datetime.now().year

# The version info for the project you're documenting, acts as replacement for
# |version| and |release|, also used in various other places throughout the
# built documents.
#
# The X.Y.Z version
# The X.Y short-version
# The full version, including alpha/beta/rc tags, or release version.
version, short_version, release = get_sdk_version()

# The language for content autogenerated by Sphinx. Refer to documentation
# for a list of supported languages.
language = 'en_CDAP'
locale_dirs = ['_locale/', common_dir_redirect + '_locale']

# A string of reStructuredText that will be included at the end of every source file that
# is read. This is the right place to add substitutions that should be available in every
# file. 
rst_epilog = """
.. role:: gp
.. |$| replace:: :gp:`$`

.. |http:| replace:: http:

.. |(TM)| unicode:: U+2122 .. trademark sign
   :ltrim:

.. |(R)| unicode:: U+00AE .. registered trademark sign
   :ltrim:

.. |--| unicode:: U+2013   .. en dash
.. |---| unicode:: U+2014  .. em dash, trimming surrounding whitespace
   :trim:

"""
if version:
    rst_epilog = rst_epilog + """
.. |bold-version| replace:: **%(version)s**

.. |italic-version| replace:: *%(version)s*

.. |literal-version| replace:: ``%(version)s``

""" % {'version': version}
if short_version:
    rst_epilog = rst_epilog + """
.. |short-version| replace:: %(short_version)s

""" % {'short_version': short_version}
if release:
    rst_epilog = rst_epilog + """
.. |literal-release| replace:: ``%(release)s``
""" % {'release': release}
if copyright:
    rst_epilog = rst_epilog + """
.. |copyright| replace:: %(copyright)s

""" % {'copyright': copyright}

# There are two options for replacing |today|: either, you set today to some
# non-false value, then it is used:
#today = ''
# Else, today_fmt is used as the format for a strftime call.
#today_fmt = '%B %d, %Y'

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
exclude_patterns = ['_examples', '_includes']

# The reST default role (used for this markup: `text`) to use for all
# documents.
#default_role = None

# If true, '()' will be appended to :func: etc. cross-reference text.
#add_function_parentheses = True

# If true, the current module name will be prepended to all description
# unit titles (such as .. function::).
#add_module_names = True

# If true, sectionauthor and moduleauthor directives will be shown in the
# output. They are ignored by default.
#show_authors = False

# The name of the Pygments (syntax highlighting) style to use.
pygments_style = 'sphinx'

# The default language to highlight source code in.
highlight_language = 'java'

# A list of ignored prefixes for module index sorting.
#modindex_common_prefix = []

# If true, keep warnings as "system message" paragraphs in the built documents.
#keep_warnings = False

# Add Google Analytics ID, or over-ride on the command line with 
# -D googleanalytics_id=UA-999-999-999
googleanalytics_id = 'UA-123-123-123'

# True by default, use it to turn off tracking.
# -D googleanalytics_enabled=1
# Turned off so unless the code and flag are passed on the command line, tracking is off.
googleanalytics_enabled = False

# -- Options for HTML output ----------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#html_theme = 'default'
#html_theme = 'nature'
#html_style = 'style.css'
#html_theme = 'cdap'
html_theme = 'cask'

# Theme options are theme-specific and customize the look and feel of a theme
# further.  For a list of options available for each theme, see the
# documentation.
#
# html_theme_options = {"showtoc_include_showtocs":"false"}
# manuals and manual_titles are lists of the manuals in the doc set
#
# versions points to the JSON file on the webservers
# versions_data is used to generate the JSONP file at http://docs.cask.co/cdap/json-versions.js
# format is a dictionary, with "development" and "older" lists of lists, and "current" a list, 
# the inner-lists being the directory and a label
#
# manual_list is an ordered list of the manuals
# Fields: directory, manual name, icon 
# icon: "" for none, "new-icon" for the ico_new.png

manuals_dict = {}
manual_titles_list = []
manual_dirs_list  = []
manual_icons_list = []
manuals_list = []

if CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP:
    manuals_list = [
        ["developers-manual",   u"Developers’ Manual",             "",],
        ["admin-manual",        "Administration Manual",           "",],
        ["integrations",        "Integrations",                    "",],
        ["examples-manual",     "Examples, Guides, and Tutorials", "",],
        ["reference-manual",    "Reference Manual",                "",],
    ]

for manual in manuals_list:
    manuals_dict[manual[0]]= manual[1]
    manual_dirs_list.append(manual[0])
    manual_titles_list.append(manual[1])
    manual_icons_list.append(manual[2])

if CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP:
    html_theme_options = {
      "manuals": manual_dirs_list,
      "manual_titles": manual_titles_list,
      "manual_icons": manual_icons_list,
      "versions":"http://docs.cask.co/cdap/json-versions.js",
      "versions_data":
        { "development": 
            [ ["2.8.0-SNAPSHOT", "2.8.0"], ], 
          "current": ["2.7.1", "2.7.1"], 
          "older": 
            [ ["2.6.1", "2.6.1"],["2.6.0", "2.6.0"],["2.5.2", "2.5.2"], ["2.5.1", "2.5.1"], ["2.5.0", "2.5.0"], ], 
        },
    }

def get_manuals():
    return html_theme_options["manuals"]

def get_manual_titles():
    return html_theme_options["manual_titles"]

def get_manual_titles_bash():
    PREFIX = "declare -a MANUAL_TITLES=("
    SUFFIX = ");"
    manual_titles = PREFIX
    for title in html_theme_options["manual_titles"]:
        manual_titles += "'%s'" % title
    manual_titles += SUFFIX
    return manual_titles 

def get_json_versions():
    return "versionscallback(%s);" % html_theme_options["versions_data"]

def print_json_versions():
    print "versionscallback(%s);" % html_theme_options["versions_data"]

def print_json_versions_file():
    head, tail = os.path.split(html_theme_options["versions"])
    print tail

# Add any paths that contain custom themes here, relative to this directory.
html_theme_path = ['_themes', common_dir_redirect + '_themes']

# The name for this set of Sphinx documents.  If None, it defaults to
# "<project> v<release> documentation".
#html_title = None

# A shorter title for the navigation bar.  Default is the same as html_title.
if CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP:
    html_short_title = u"CDAP Documentation v%s" % version

# A shorter title for the sidebar section, preceding the words "Table of Contents".
html_short_title_toc = u"Documentation"
if CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP:
    html_short_title_toc = u"CDAP Documentation"

# The name of an image file (relative to this directory) to place at the top
# of the sidebar.
#html_logo = None

# The name of an image file (within the static path) to use as favicon of the
# docs.  This file should be a Windows icon file (.ico) being 16x16 or 32x32
# pixels large.
# html_favicon = '_static/favicon.ico'
html_favicon = common_dir_redirect + '_static/cask_favicon.ico'
if CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP or CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP_TUTORIAL :
    html_favicon = common_dir_redirect + '_static/cdap_favicon.ico'

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = [common_dir_redirect + '_static']

# Add any extra paths that contain custom files (such as robots.txt or
# .htaccess) here, relative to this directory. These files are copied
# directly to the root of the documentation.
#html_extra_path = []

# If not '', a 'Last updated on:' timestamp is inserted at every page bottom,
# using the given strftime format.
#html_last_updated_fmt = '%b %d, %Y'

# If true, SmartyPants will be used to convert quotes and dashes to
# typographically correct entities.
#html_use_smartypants = True

# Custom sidebar templates, maps document names to template names.
html_sidebars = {'**': ['globaltoc.html', 'relations.html', 'searchbox.html', ],}
if CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP:
    html_sidebars = {'**': ['manuals.html', 'globaltoc.html', 'relations.html', 'downloads.html', 'searchbox.html', ],}

# Additional templates that should be rendered to pages, maps page names to
# template names.
#html_additional_pages = {}

# If false, no module index is generated.
#html_domain_indices = True

# If false, no index is generated.
#html_use_index = True

# If true, the index is split into individual pages for each letter.
#html_split_index = False

# If true, links to the reST sources are added to the pages.
html_show_sourcelink = False
html_copy_source = False

# If true, "Created using Sphinx" is shown in the HTML footer. Default is True.
html_show_sphinx = False

# If true, "(C) Copyright ..." is shown in the HTML footer. Default is True.
#html_show_copyright = True

# If true, an OpenSearch description file will be output, and all pages will
# contain a <link> tag referring to it.  The value of this option must be the
# base URL from which the finished HTML is served.
#html_use_opensearch = ''

# This is the file name suffix for HTML files (e.g. ".xhtml").
#html_file_suffix = None

# Output file base name for HTML help builder.
htmlhelp_basename = 'CDAPdoc'

# This context needs to be created in each child conf.py. At a minimum, it needs to be 
# html_context = {"html_short_title_toc":html_short_title_toc}
# This is because it needs to be set as the last item.
html_context = {"html_short_title_toc":html_short_title_toc}

# -- Options for LaTeX output ---------------------------------------------

latex_elements = {
# The paper size ('letterpaper' or 'a4paper').
#'papersize': 'letterpaper',

# The font size ('10pt', '11pt' or '12pt').
#'pointsize': '10pt',

# Additional stuff for the LaTeX preamble.
#'preamble': '',
}

# Grouping the document tree into LaTeX files. List of tuples
# (source start file, target name, title,
#  author, documentclass [howto, manual, or own class]).
latex_documents = [
  ('index', 'Cask.tex', u'Cask Data Documentation',
   u'Cask Data, Inc.', 'manual'),
]

# The name of an image file (relative to this directory) to place at the top of
# the title page.
#latex_logo = None

# For "manual" documents, if this is true, then toplevel headings are parts,
# not chapters.
#latex_use_parts = False

# If true, show page references after internal links.
#latex_show_pagerefs = False

# If true, show URL addresses after external links.
#latex_show_urls = False

# Documents to append as an appendix to all manuals.
#latex_appendices = []

# If false, no module index is generated.
#latex_domain_indices = True


# -- Options for manual page output ---------------------------------------

# One entry per manual page. List of tuples
# (source start file, name, description, authors, manual section).
man_pages = [
    ('index', 'cdap', u'Cask Data Documentation',
     [u'Cask Data, Inc.'], 1)
]

# If true, show URL addresses after external links.
#man_show_urls = False


# -- Options for Texinfo output -------------------------------------------

# Grouping the document tree into Texinfo files. List of tuples
# (source start file, target name, title, author,
#  dir menu entry, description, category)
texinfo_documents = [
  ('index', 'CDAP', u'Cask Data Documentation',
   u'Cask Data, Inc.', 'CaskData', 'Cask Data Application Platform',
   'Miscellaneous'),
]

# Documents to append as an appendix to all manuals.
#texinfo_appendices = []

# If false, no module index is generated.
#texinfo_domain_indices = True

# How to display URL addresses: 'footnote', 'no', or 'inline'.
#texinfo_show_urls = 'footnote'

# If true, do not generate a @detailmenu in the "Top" node's menu.
#texinfo_no_detailmenu = False


# Example configuration for intersphinx: refer to the Python standard library.
#intersphinx_mapping = {'http://docs.python.org/': None}

# -- Options for PDF output --------------------------------------------------

# Grouping the document tree into PDF files. List of tuples
# (source start file, target name, title, author, options).
#
# If there is more than one author, separate them with \\.
# For example: r'Guido van Rossum\\Fred L. Drake, Jr., editor'
#
# The options element is a dictionary that lets you override
# this config per-document.
# For example,
# ('index', u'MyProject', u'My Project', u'Author Name',
#  dict(pdf_compressed = True))
# would mean that specific document would be compressed
# regardless of the global pdf_compressed setting.

pdf_documents = [
    ('index', u'CaskData', u'Cask Data Documentation', u'Cask Data, Inc.'),
]
if CASK_PROJECT_TYPE == CASK_PROJECT_TYPE_CDAP:
    pdf_documents = [
        ('index', u'CDAP', u'Cask Data Application Platform', u'Cask Data, Inc.'),
    ]

# A comma-separated list of custom stylesheets. Example:
#pdf_stylesheets = ['sphinx','kerning','a4']
pdf_stylesheets = ['pdf-stylesheet']

# A list of folders to search for stylesheets. Example:
pdf_style_path = ['.', '_templates', '_styles']

# Create a compressed PDF
# Use True/False or 1/0
# Example: compressed=True
#pdf_compressed = False

# A colon-separated list of folders to search for fonts. Example:
# pdf_font_path = ['/usr/share/fonts', '/usr/share/texmf-dist/fonts/']
pdf_font_path = ['/Library/fonts', '~/Library/Fonts']

# Language to be used for hyphenation support
pdf_language = "en_US"

# Mode for literal blocks wider than the frame. Can be
# overflow, shrink or truncate
pdf_fit_mode = "shrink"

# Section level that forces a break page.
# For example: 1 means top-level sections start in a new page
# 0 means disabled
pdf_break_level = 1

# When a section starts in a new page, force it to be 'even', 'odd',
# or just use 'any'
pdf_breakside = 'any'

# Insert footnotes where they are defined instead of
# at the end.
#pdf_inline_footnotes = True

# verbosity level. 0 1 or 2
#pdf_verbosity = 0

# If false, no index is generated.
pdf_use_index = False

# If false, no modindex is generated.
pdf_use_modindex = False

# If false, no coverpage is generated.
pdf_use_coverpage = False

# Name of the cover page template to use
#pdf_cover_template = 'sphinxcover.tmpl'

# Documents to append as an appendix to all manuals.
#pdf_appendices = []

# Enable experimental feature to split table cells. Use it
# if you get "DelayedTable too big" errors
#pdf_splittables = False

# Set the default DPI for images
#pdf_default_dpi = 72

# Enable rst2pdf extension modules (default is only vectorpdf)
# you need vectorpdf if you want to use sphinx's graphviz support
#pdf_extensions = ['vectorpdf']

# Page template name for "regular" pages
#pdf_page_template = 'cutePage'

# Show Table Of Contents at the beginning?
#pdf_use_toc = True

# How many levels deep should the table of contents be?
pdf_toc_depth = 9999

# Add section number to section references
pdf_use_numbered_links = False

# Background images fitting mode
pdf_fit_background_mode = 'scale'
