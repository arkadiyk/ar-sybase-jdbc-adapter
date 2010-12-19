require 'helper'
require 'arel/visitors/sybase_jtds'

module Arel
  module Visitors
    describe 'the oracle visitor' do
      before do
        @visitor = SybaseJtds.new Table.engine
      end


      describe 'Nodes::SelectStatement' do
        describe 'limit' do
          it 'adds a rownum clause' do
            stmt = Nodes::SelectStatement.new
            puts stmt

            stmt.limit = 10
            sql = @visitor.accept stmt
            puts sql

            sql.must_be_like %{ SELECT WHERE ROWNUM <= 10 }
          end

        end

#
#        describe 'only offset' do
#          it 'creates a select from subquery with rownum condition' do
#            stmt = Nodes::SelectStatement.new
#            stmt.offset = Nodes::Offset.new(10)
#            sql = @visitor.accept stmt
#            sql.must_be_like %{
#              SELECT * FROM (
#                SELECT raw_sql_.*, rownum raw_rnum_
#                FROM (SELECT ) raw_sql_
#              )
#              WHERE raw_rnum_ > 10
#            }
#          end
#        end

      end
    end
  end
end
