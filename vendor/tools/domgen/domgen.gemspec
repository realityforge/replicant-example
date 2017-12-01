# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name               = %q{domgen}
  s.version            = '0.19.0.dev'
  s.platform           = Gem::Platform::RUBY

  s.authors            = ['Peter Donald']
  s.email              = %q{peter@realityforge.org}

  s.homepage           = %q{https://github.com/realityforge/domgen}
  s.summary            = %q{A tool to generates code from a simple domain model.}
  s.description        = %q{A tool to generates code from a simple domain model.}

  s.rubyforge_project  = %q{domgen}

  s.files              = `git ls-files`.split("\n")
  s.test_files         = `git ls-files -- {spec}/*`.split("\n")
  s.executables        = `git ls-files -- bin/*`.split("\n").map { |f| File.basename(f) }
  s.default_executable = []
  s.require_paths      = %w(lib)

  s.has_rdoc           = false
  s.rdoc_options       = %w(--line-numbers --inline-source --title domgen)

  s.add_dependency 'reality-core', '>= 1.8.0'
  s.add_dependency 'reality-facets', '>= 1.11.0'
  s.add_dependency 'reality-generators', '>= 1.6.0'
  s.add_dependency 'reality-naming', '>= 1.9.0'
  s.add_dependency 'reality-orderedhash', '>= 1.0.0'
  s.add_dependency 'reality-mash', '>= 1.0.0'
end
