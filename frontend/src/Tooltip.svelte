<script>
    import { createEventDispatcher } from 'svelte';
    import IconButton from "./IconButton.svelte";
    import { isPortraitOrientation } from "./common/dom";

    export let open = false;
    export let text;
    export let showConfirm = true;
    export let showReject = true;
    export let up = true;
    export let left = false;
    export let showArrow = true;

    const confirmTitle = 'Готово';
    const rejectTitle = 'Закрыть';

    const dispatch = createEventDispatcher();

    let portrait = isPortraitOrientation();
    $: width = portrait ? "6rem" : "15rem";
    $: btnSz = portrait ? "25" : "30";
    $: iconSz = portrait ? "8" : "10";

    window.addEventListener('orientationchange', function() {
        console.log('ORIENT', window.innerHeight, window.innerWidth)
        portrait = !isPortraitOrientation();
    }, false);

    const onConfirm = () => {
        dispatch('confirm');
    };

    const onReject = () => {
        dispatch('reject');
    };

    const onKeyDown = (e) => {
        if (open) {
            if (e.key === 'Escape') {
                console.log('esc')
            } else if (e.key === 'Enter') {
                console.log('ent')
            }
        }
    };

</script>

{#if open}
    <div
            style="--width: {width};"
            class="tooltip"
            class:up={up}
            class:left={left}
            on:keydown={onKeyDown}>

        {#if showArrow}
            <div class="tooltip-arrow"></div>
        {/if}
        {text}
        <div class="tooltip-bar">
            {#if showConfirm}
                <div class="tooltip-button left">
                    <IconButton icon="check" iconSize={iconSz} height={btnSz} width={btnSz}
                                title={confirmTitle} opacity={1} hoverOpacity={0.7}
                                round fill color="white" backColor="#4fd173"
                                on:click={onConfirm}/>
                </div>
            {/if}
            {#if showReject}
                <div class="tooltip-button">
                    <IconButton icon="cross" iconSize={iconSz} height={btnSz} width={btnSz}
                                title={rejectTitle} opacity={1} hoverOpacity={0.7}
                                round borders
                                on:click={onReject}/>
                </div>
            {/if}
        </div>
    </div>
{/if}

<style lang="scss">
  @import './styles/_mixins';

  .tooltip {
    --color: #ffffff;

    position: absolute;
    //transform: translate(0, 100%);
    top: 0;
    left: -0.25rem;
    min-width: var(--width);
    background-color: var(--color);
    padding: 0.75rem 0.25rem 0.25rem 0.75rem;
    box-shadow: 0 4px 6px 0 #b7b1ac;
    text-align: left;
    font-size: .8rem;
    z-index: 2;
    box-sizing: border-box;
    border-radius: 0.25rem;

    &.left {
      transform: translateX(-100%);
    }

    & .tooltip-arrow {
      &::before {
        border-width: 0.6rem 0.45rem 0 0.45rem;
        border-left-color: transparent;
        border-right-color: transparent;
        top: calc(100%);
        content: '';
        display: block;
        width: 0;
        height: 0;
        border-style: solid;
        position: absolute;
        color: var(--color);
      }
    }

    &.up {
      transform: translateY(-100%);
    }

    &:not(.up) {
      top: 1.5rem;

      & .tooltip-arrow {
        top: calc(0rem - 0.7rem);
        left: calc(100% - 1.95rem);

        &::before {
          border-width: 0 0.45rem 0.6rem 0.45rem;
          top: -0.6rem;
          left: 0.6rem;
          border-left-color: transparent;
          border-right-color: transparent;
          content: '';
          display: block;
          width: 0;
          height: 0;
          border-style: solid;
          position: absolute;
          color: var(--color);
        }
      }
    }

    & .tooltip-bar {
      display: flex;
      justify-content: center;
      margin-top: 0.75rem;
      margin-bottom: 0.3rem;
    }

    & .tooltip-button {
      &.left {
        margin-right: 1rem;
      }
    }
  }

  @include portrait {
    .tooltip {
      font-size: x-small;
      line-height: 0.75rem;
      padding: 0.25rem;
    }

    .tooltip-bar {
      margin-top: 0.5rem !important;
      margin-bottom: 0.125rem !important;
      justify-content: flex-start !important;
    }
  }

</style>