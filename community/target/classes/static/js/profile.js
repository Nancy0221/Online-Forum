$(function () {
  $(".follow-btn").click(follow);
});

function follow() {
  var btn = this;
  if ($(btn).hasClass("btn-info")) {
    // 关注TA
    $.post(
      CONTEXT_PATH + "/follow",
      {
        entityType: 3,
        // 获取当前按钮的上一个节点<input>的值，去获取entityId
        entityId: $(btn).prev().val(),
      },
      // 处理返回结果
      function (data) {
        // 转成JS对象
        data = $.parseJSON(data);
        if (data.code == 0) {
          // 刷新页面
          window.location.reload();
        } else {
          alert(data.msg);
        }
      }
    );
    // $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
  } else {
    $.post(
      CONTEXT_PATH + "/unfollow",
      {
        entityType: 3,
        entityId: $(btn).prev().val(),
      },
      function (data) {
        data = $.parseJSON(data);
        if (data.code == 0) {
          window.location.reload();
        } else {
          alert(data.msg);
        }
      }
    );
    // 取消关注
    // $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
  }
}
