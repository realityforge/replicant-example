require 'dbt'
require 'domgen'

Domgen::Build.define_load_task

Domgen::Build.define_generate_task([:pgsql], :key => :sql, :target_dir => 'database/generated')

Dbt::Config.driver = 'postgres'

Dbt.add_database(:default) do |database|
  database.search_dirs = %w(database/generated database)
  database.enable_domgen
  database.version = '1'
end
