import { message } from 'ant-design-vue'

// ant-design-vue 4.x 静态方法包装
export const showMessage = {
  success: (content) => {
    message.success(content)
  },
  error: (content) => {
    message.error(content)
  },
  warning: (content) => {
    message.warning(content)
  },
  info: (content) => {
    message.info(content)
  }
}

export default showMessage
