<template>
  <div class="my-dorm-page">
    <section class="dorm-info-card">
      <div class="dorm-icon">⌂</div>
      <h1>{{ dormName }}</h1>
      <div class="chips"><span>{{ bedCount }}人间</span><span>{{ occupancyStatus }}</span><b>{{ myBed }}号床位</b></div>
      <dl>
        <dt>入住日期</dt><dd>{{ checkinDate }}</dd>
        <dt>床位信息</dt><dd>{{ myBed }}号床</dd>
        <dt>责任宿管</dt><dd>{{ dormKeeper }}</dd>
      </dl>
      <div class="split"></div>
      <h2>我的室友 <small>{{ roommates.length }}/{{ bedCount }}</small></h2>
      <div class="roommate-list">
        <div v-for="mate in roommates" :key="mate.name" :class="{ selected: mate.tag }">
          <span class="avatar-img" :style="{ background: mate.color }">{{ mate.avatar }}</span>
          <div><b>{{ mate.name }} <em v-if="mate.tag">{{ mate.tag }}</em></b><p>{{ mate.bed }}　{{ mate.status }}</p></div>
          <i>☻</i>
        </div>
      </div>
    </section>

    <section class="bed-layout content-card">
      <div class="card-title"><h2>宿舍床位布局</h2></div>
      <div class="layout-board">
        <span class="door">大 门</span>
        <div class="room-area">
          <div class="bed-row" v-for="row in bedRows" :key="row[0].bed">
            <div v-for="b in row" :key="b.bed"
                 :class="['bed', 'bed-' + b.bed, { mine: b.tag }]">
              <b>{{ b.bed }}号</b><span>{{ b.name }}</span>
            </div>
          </div>
        </div>
        <span class="balcony">阳 台</span>
      </div>
    </section>

    <section class="status-card content-card">
      <div class="status-split">
        <div class="status-left">
          <div class="card-title"><h2>设施运行状态</h2></div>
          <div class="facility-list">
            <div v-for="fac in facilities" :key="fac.name" class="facility-row">
              <span :class="['fac-dot', fac.status === 'normal' ? 'ok' : 'warn']">{{ fac.icon }}</span>
              <div><b>{{ fac.name }}</b><p>{{ fac.desc }}</p></div>
              <em :class="fac.status === 'normal' ? 'ok' : 'warn'">{{ fac.label }}</em>
            </div>
          </div>
        </div>
        <div class="status-divider"></div>
        <div class="status-right">
          <div class="card-title"><h2>卫生月度指标</h2></div>
          <div class="dorm-score">
            <div class="ds-circle">{{ hygieneScore }}<span>分</span></div>
            <div class="ds-rows">
              <p>全楼排名 <b>第 {{ rankNum }} 名</b></p>
              <p>上次检查 <b>{{ lastCheckScore }} · {{ lastCheckResult }}</b></p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'MyDorm',
  props: {
    roommates: { type: Array, default: () => [] },
    dormName: { type: String, default: '芙蓉楼3 · 519室' },
    bedCount: { type: Number, default: 4 },
    occupancyStatus: { type: String, default: '已住满' },
    myBed: { type: Number, default: 2 },
    checkinDate: { type: String, default: '2025/9/1' },
    dormKeeper: { type: String, default: '张宿管' },
    hygieneScore: { type: Number, default: 95 },
    rankNum: { type: Number, default: 12 },
    lastCheckScore: { type: Number, default: 98 },
    lastCheckResult: { type: String, default: '优秀' },
    facilities: { type: Array, default: () => [] }
  },
  computed: {
    bedRows() {
      const r = this.roommates
      if (r.length >= 4) {
        // Layout: Row1 (near door): 4号 | 1号, Row2 (near balcony): 3号 | 2号
        return [
          [r[3], r[0]],
          [r[2], r[1]]
        ]
      }
      const rows = []
      for (let i = 0; i < r.length; i += 2) {
        rows.push(r.slice(i, i + 2))
      }
      return rows
    }
  }
}
</script>
