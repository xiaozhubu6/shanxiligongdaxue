<template>
  <div class="announcement-container">
    <div class="header">
      <h3>公告管理</h3>
      <el-button type="primary" @click="handleAdd">发布新公告</el-button>
    </div>
    
    <el-table :data="announcementList" style="width: 100%" border>
      <el-table-column prop="content" label="公告内容" width="400"></el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180">
        <template slot-scope="scope">
          {{ scope.row.createTime | formatDate }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template slot-scope="scope">
          <el-button size="mini" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="mini" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          <el-button 
            size="mini" 
            v-if="scope.row.status === 1" 
            @click="toggleStatus(scope.row, 0)"
            type="warning"
          >
            禁用
          </el-button>
          <el-button 
            size="mini" 
            v-else 
            @click="toggleStatus(scope.row, 1)"
            type="success"
          >
            启用
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 发布/编辑公告对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="50%">
      <el-form :model="form" ref="form" label-width="80px">
        <el-form-item label="公告内容" prop="content">
          <el-input type="textarea" v-model="form.content" :rows="4" placeholder="请输入公告内容"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'AnnouncementManagement',
  data() {
    return {
      announcementList: [],
      dialogVisible: false,
      form: {
        id: null,
        content: '',
        status: 1
      },
      isEdit: false
    };
  },
  computed: {
    dialogTitle() {
      return this.isEdit ? '编辑公告' : '发布新公告';
    }
  },
  created() {
    this.fetchAnnouncementList();
  },
  methods: {
    async fetchAnnouncementList() {
      try {
        const response = await new this.Request(this.Urls.m().getAnnouncementList, {}).modeget();
        console.log('公告列表响应:', response);
        if (response.data && Array.isArray(response.data)) {
          this.announcementList = response.data;
        } else {
          this.announcementList = [];
        }
      } catch (error) {
        console.error('获取公告列表失败:', error);
        this.$message.error('获取公告列表失败');
        this.announcementList = [];
      }
    },
    handleAdd() {
      this.isEdit = false;
      this.form = { id: null, content: '', status: 1 };
      this.dialogVisible = true;
    },
    handleEdit(row) {
      this.isEdit = true;
      this.form = { ...row };
      this.dialogVisible = true;
    },
    async toggleStatus(row, status) {
      try {
        // 更新本地数据状态
        row.status = status;
        
        const params = { ...row };
        const response = await new this.Request(this.Urls.m().updateAnnouncement, params).modepost();
        
        if (response.data === true) {
          this.$message.success(status === 0 ? '已禁用' : '已启用');
          // 刷新列表以确保状态同步
          this.fetchAnnouncementList();
        } else {
          // 如果更新失败，恢复原状态
          row.status = row.status === 0 ? 1 : 0;
          this.$message.error('状态更新失败');
        }
      } catch (error) {
        console.error('更新状态失败:', error);
        // 如果更新失败，恢复原状态
        row.status = row.status === 0 ? 1 : 0;
        this.$message.error('状态更新失败');
      }
    },
    async handleDelete(id) {
      try {
        await this.$confirm('确定要删除该公告吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });
        const response = await new this.Request(this.Urls.m().deleteAnnouncement, { id }).modeget();
        if (response.data === true) {
          this.$message.success('删除成功');
          this.fetchAnnouncementList();
        } else {
          this.$message.error('删除失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除公告失败:', error);
          this.$message.error('删除失败');
        }
      }
    },
    async submitForm() {
      try {
        let response;
        if (this.isEdit) {
          response = await new this.Request(this.Urls.m().updateAnnouncement, this.form).modepost();
        } else {
          response = await new this.Request(this.Urls.m().addAnnouncement, this.form).modepost();
        }
        
        if (response.data === true) {
          this.$message.success(this.isEdit ? '更新成功' : '发布成功');
          this.dialogVisible = false;
          this.fetchAnnouncementList();
        } else {
          this.$message.error('操作失败');
        }
      } catch (error) {
        console.error('操作失败:', error);
        this.$message.error('操作失败');
      }
    }
  },
  filters: {
    formatDate(value) {
      if (!value) return '';
      const date = new Date(value);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }
  }
};
</script>

<style scoped>
.announcement-container {
  margin-left: 200px; /* 避免覆盖左侧菜单 */
  padding: 20px;
  min-height: calc(100vh - 100px); /* 确保最小高度 */
  padding-top: 60px; /* 避免被顶部导航栏遮挡 */
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.el-table {
  width: 100% !important;
  margin-top: 20px;
}
/* 确保表格内容铺满整个区域 */
.announcement-container .el-table__body-wrapper {
  width: 100% !important;
}
</style>