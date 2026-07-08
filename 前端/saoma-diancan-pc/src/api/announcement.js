// 公告管理API接口

// 获取公告列表
export function getAnnouncementList() {
  return new Promise((resolve, reject) => {
    new this.Request(this.Urls.m().getAnnouncementList, {}).modeget()
      .then(res => resolve(res))
      .catch(err => reject(err));
  });
}

// 添加公告
export function addAnnouncement(data) {
  return new Promise((resolve, reject) => {
    new this.Request(this.Urls.m().addAnnouncement, data).modepost()
      .then(res => resolve(res))
      .catch(err => reject(err));
  });
}

// 更新公告
export function updateAnnouncement(data) {
  return new Promise((resolve, reject) => {
    new this.Request(this.Urls.m().updateAnnouncement, data).modepost()
      .then(res => resolve(res))
      .catch(err => reject(err));
  });
}

// 删除公告
export function deleteAnnouncement(id) {
  return new Promise((resolve, reject) => {
    new this.Request(this.Urls.m().deleteAnnouncement, { id }).modeget()
      .then(res => resolve(res))
      .catch(err => reject(err));
  });
}