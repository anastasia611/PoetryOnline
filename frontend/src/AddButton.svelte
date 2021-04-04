<script>
    export let title = 'Remove icon';
    export let size = 8;
</script>

<!--TODO: move to button-->
<label for="next-line"></label>
<button id="next-line" on:click>+</button>

<style lang="scss">
    #next-line {
        width: 1.5rem;
        height: 1.5rem;
        line-height: 1rem;

        margin-left: 0.75rem;
        padding: 0;

        border-radius: 1.5rem;
        border: none;

        font-size: 1.25rem;
        font-weight: 900;

        color: #500808;
        opacity: 0.2;

        &:hover {
            background: none;
            opacity: 0.75;
        }
        &:focus {
            background: none;
        }
    }
</style>
