/**
 * 基础设施模块国际化文件汇总（中文）
 */
import config from './config'
import apiAccessLog from './apiAccessLog'
import apiErrorLog from './apiErrorLog'
import codegen from './codegen'
import dataSourceConfig from './dataSourceConfig'
import file from './file'
import fileConfig from './fileConfig'
import job from './job'
import redis from './redis'
import server from './server'
import build from './build'

export default {
  infra: {
    ...config,
    ...apiAccessLog,
    ...apiErrorLog,
    ...codegen,
    ...dataSourceConfig,
    ...file,
    ...fileConfig,
    ...job,
    ...redis,
    ...server,
    ...build
  }
}
