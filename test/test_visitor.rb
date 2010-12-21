require 'helper'
require 'arel/visitors/sybase_jtds'

module Arel
  module Visitors
    describe 'the sybase jtds visitor' do
      before do
        @visitor = SybaseJtds.new Table.engine
      end


      describe Nodes::SelectStatement do
        it "should not have 'LIMIT' keyword" do
          stmt = Nodes::SelectStatement.new
          stmt.cores.first.projections << 'first_field'
          stmt.limit = 10
          sql = @visitor.accept stmt
          sql.wont_match /LIMIT 10/
        end

        describe 'limit with no offset and no "DISTINCT"' do
          it 'adds a TOP keyword after "SELECT"' do
            stmt = Nodes::SelectStatement.new
            stmt.cores.first.projections << 'first_field'
            stmt.limit = 10
            sql = @visitor.accept stmt
            sql.must_be_like %{ SELECT TOP 10 'first_field' }
          end
        end

        describe 'limit with no offset and "DISTINCT"' do
          it 'adds a TOP keyword after "DISTINCT"' do
            stmt = Nodes::SelectStatement.new
            stmt.cores.first.projections << Nodes::SqlLiteral.new('DISTINCT id')
            stmt.limit = 10
            sql = @visitor.accept stmt
            sql.must_be_like %{ SELECT DISTINCT TOP 10 id }
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
