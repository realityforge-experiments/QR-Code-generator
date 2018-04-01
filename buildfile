require 'buildr/git_auto_version'
require 'buildr/gpg'
require 'buildr/gwt'

desc 'GWT QR Code Generation'
define 'gwt-qr-code' do
  project.group = 'org.realityforge.gwt.qr_code'
  compile.options.source = '1.8'
  compile.options.target = '1.8'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  compile.with :javax_jsr305,
               :jsinterop_base,
               :jsinterop_base_sources,
               :jsinterop_annotations,
               :jsinterop_annotations_sources,
               :elemental2_core,
               :elemental2_dom,
               :elemental2_promise,
               :braincheck

  gwt_enhance(project)

  test.using :testng

  package(:jar)
  package(:sources)
  package(:javadoc)

  doc.
    using(:javadoc,
          :windowtitle => 'React4j Widget API Documentation',
          :linksource => true,
          :timestamp => false,
          :link => %w(https://docs.oracle.com/javase/8/docs/api)
    )

  iml.excluded_directories << project._('tmp')

  ipr.add_component_from_artifact(:idea_codestyle)
end
